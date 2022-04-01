package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import gg.bridgesyndicate.util.Seconds;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class SqsGameDataPoller implements GameDataPoller{

    private void quitAfterTimeout(Plugin plugin){
        new BukkitRunnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }.runTaskLater(plugin, Seconds.toTicks(5.0f));
    }

    private void abortGameOnTimeout(Plugin plugin, Game game) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getState() == Game.GameState.AWAITING_PLAYERS) {
                    System.out.println("Game has not started in " + NO_START_ABORT_TIME_IN_SECONDS
                            + " seconds. Aborting.");
                    game.setState(Game.GameState.ABORTED);
                    try {
                        HttpClient.put(game, HttpClient.PUT_REASONS.ABORTED_GAME);
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                    quitAfterTimeout(plugin);
                }
            }
        }.runTaskLater(plugin, Seconds.toTicks(NO_START_ABORT_TIME_IN_SECONDS));
    }

    private Regions determineRegion() {
        DefaultAwsRegionProviderChain defaultAwsRegionProviderChain = new DefaultAwsRegionProviderChain();
        String sqsRegion = defaultAwsRegionProviderChain.getRegion();
        final String SYNDICATE_MATCH_QUEUE_REGION = "SYNDICATE_MATCH_QUEUE_REGION";
        if ( System.getenv(SYNDICATE_MATCH_QUEUE_REGION) != null ) {
            sqsRegion = System.getenv(SYNDICATE_MATCH_QUEUE_REGION);
            System.out.println("Using " + SYNDICATE_MATCH_QUEUE_REGION + " from env: " + sqsRegion);
        } else {
            System.out.println("Using default " + SYNDICATE_MATCH_QUEUE_REGION + ": " + sqsRegion);
        }
        return Regions.fromName(sqsRegion);
    }

    @Override
    public void poll(BridgeTeams bridgeTeams) {
        final String QUEUE_ENV_NAME = "SYNDICATE_MATCH_QUEUE_NAME";
        AmazonSQS sqs;
        try {
            sqs = AmazonSQSClientBuilder.standard()
                    .withRegion(determineRegion())
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .build();
        } catch (Exception e) {
            System.out.println("Error building an SQS client. This container will never poll for a game.");
            if (SyndicateEnvironment.SYNDICATE_ENV() == Environments.PRODUCTION) {
                System.out.println("Exiting with error: Can not start sqs polling.");
                System.exit(-1);
            }
            return;
        }
        final String QUEUE_NAME = System.getenv(QUEUE_ENV_NAME);
        System.out.println("Polling: " + QUEUE_NAME);
        if (QUEUE_NAME == null) {
            System.out.println("Exiting with error: " + QUEUE_ENV_NAME + " environment variable is null");
            System.exit(-1);
        }
        AmazonSQS finalSqs = sqs;
        new BukkitRunnable() {
            @Override
            public void run() {
                final String queueUrl = finalSqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
                ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
                        .withQueueUrl(QUEUE_NAME)
                        .withWaitTimeSeconds(20);
                List<Message> messages = finalSqs.receiveMessage(receive_request).getMessages();
                if ( messages.size() > 0 ) {
                    Message message = messages.get(0);
                    System.out.println("found message on " + QUEUE_NAME + ": " + message.getBody());
                    Game game = Game.deserialize(message.getBody());
                    bridgeTeams.receiveGame(game);
                    try {
                        game.addContainerMetaData();
                        HttpClient.put(game, HttpClient.PUT_REASONS.CONTAINER_METADATA);
                        if (SyndicateEnvironment.SYNDICATE_ENV() == Environments.PRODUCTION) {
                            System.out.println("Environment is " + SyndicateEnvironment.SYNDICATE_ENV() +
                                    " aborting game after " + NO_START_ABORT_TIME_IN_SECONDS);
                            abortGameOnTimeout(bridgeTeams, game);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exiting with error: Could not add container metadata.");
                        System.exit(-1);
                    }
                    finalSqs.deleteMessage(queueUrl, message.getReceiptHandle());
                    this.cancel();
                } else {
                    ContainerMetadata containerMetadata = null;
                    try {
                        containerMetadata = new ContainerMetadata(false);
                    } catch (IOException e) {
                        System.out.println("ERROR: fetching container metadata for scale-in check. Continue polling");
                    }
                    if (containerMetadata != null) {
                        String taskArn = containerMetadata.getTaskArn();
                        try {
                            HttpClient.post(taskArn);
                            new FileOutputStream("KYS").close();
                            System.exit(0);
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                            System.out.println("ERROR: posting to scale-in or writing KYS file. Continue polling.");
                        }
                    }
                }
            }
        }.runTaskTimer(bridgeTeams, 0, Seconds.toTicks(1.0f));
    }
}
