// Use https://prettier.io/playground/ and
// https://mrcoles.com/bookmarklet/
// to make the bookmarklet

function processRow(tds) {
  var k = tds[0].innerText;
  var v = tds[1].innerText.replace("\n", " ");

  switch (k) {
    case "@log":
      var remove_account = /\d+:/;
      return "--log-group-name " + v.replace(remove_account, "");
      break;
    case "@logStream":
      return "--log-stream-names " + v;
      break;
    case "@timestamp":
      return "--start-time " + v + " --end-time " + v;
      break;
    default:
      return "";
  }
}

function createCmdForFilterLogEvents() {
  var iframe = document.getElementById("microConsole-Logs");
  var cmd = "aws logs filter-log-events ";
  var tbody = iframe.contentWindow.document
    .getElementsByClassName("logs-insights-expanded-row")[0]
    .getElementsByTagName("tbody");
  tbody[0].children.forEach(function (row) {
    cmd = cmd.concat(processRow(row.children));
  });
  cmd = cmd.concat("| jq -r '.events[].message'");
  return cmd;
}

var cmd = createCmdForFilterLogEvents();

navigator.clipboard.writeText(cmd).then(
  function () {
    console.log("success!");
  },
  function (err) {
    console.error("failure:", err);
  }
);

