javascript: (function () {
  // use prettier.io
  let rawToken = document.cookie.split(";")[7];
  rawToken = rawToken.split("access_token=")[1];
  let decodedToken = decodeURIComponent(rawToken);
  let jsonObj = JSON.parse(decodedToken);
  let accessToken = jsonObj["accessToken"];
  console.log(accessToken);
  navigator.clipboard.writeText(accessToken).then(
    function () {
      console.log("Async:Copying to clipboard was successful!");
    },
    function (err) {
      console.error("Async:Could not copy text:", err);
    }
  );
})();


