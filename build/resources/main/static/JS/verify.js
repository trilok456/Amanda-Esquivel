(function (e) {
    function checkSession() {
      const sessionId = e.sessionStorage.getItem("sessionId");
      const regex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  
      if (!sessionId || !regex.test(sessionId)) {
        window.location.href = "login.html";
        return; // Exit the function after redirect
      }
    }
  
    e.onload = checkSession;
  })(this);