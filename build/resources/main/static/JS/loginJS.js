(function(e) {
  e.onload = function() {
    // Remove previously stored session ID (if any)
    e.sessionStorage.removeItem("sessionId");

    // Fetch the intro content when the page loads
    fetch('/messages/intro')
      .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch intro content');
        }
        return response.text();
      })
      .then(content => {
        // Update the intro element with the fetched content
        document.getElementById("intro").textContent = content;
      })
      .catch(error => {
        console.error("Error fetching intro content:", error);
        document.getElementById("intro").textContent = "Connection Error!";
      });
  };

  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");
  const usernameError = document.getElementById("usernameError");  

  const passwordError = document.getElementById("passwordError");  

  const submitButton = document.getElementById("submitButton");

  submitButton.addEventListener("click", function(event) {
    event.preventDefault();  
 // Prevent default form submission

    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    // Clear previous error messages
    usernameError.textContent = "";
    passwordError.textContent = "";

    // Validate username length
    if (username.length !== 10) {
      usernameError.textContent = "Username must be 10 characters long.";
      return; // Prevent form submission if validation fails
    }

    // Validate password length
    if (password.length !== 16) {
      passwordError.textContent = "Password must be 16 characters long.";
      return; // Prevent form submission if validation fails
    }

    // Submit the form with trimmed username and password
    const formData = new URLSearchParams();
    formData.append("userName", username);
    formData.append("password", password);  


    fetch("/loginForm", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: formData
    })
    .then(response => {
      if (response.ok) {
        return response.json();
      }
      throw new Error("Login failed");
    })
    .then(data => {
      if (data.status === 200) { // Login successful
        sessionStorage.setItem("sessionId", data.sessionId);
        window.location.href = data.redirectUrl;
      } else { // Handle errors (PAYMENT_REQUIRED or UNAUTHORIZED)
        let errorMessage;
        if (data.status === 402) { // PAYMENT_REQUIRED
          errorMessage = data.message;
        } else if (data.status === 401) { // UNAUTHORIZED
          errorMessage = data.message;
        } else {
          errorMessage = "Login failed. Please try again.";
        }

        // Display an alert popup with the appropriate error message
        alert(errorMessage);
      }
    })
    .catch(error => {
      console.error("Error:", error);
      alert("An unexpected error occurred. Please try again.");
    });
  });
})(this);