
$(document).ready(function(){
	
	confirmPasswordMatch();

});


function confirmPasswordMatch(){
	
	$("#newPassword, #confirmNewPassword").keyup( function(){
		
		var password = $("#newPassword").val();
		var confirmPassword = $("#confirmNewPassword").val();
		
		if(password == "" && confirmPassword == ""){
			$("#message").empty();
			$("#submit").prop("disabled", true);
		}
		
		else if(password == confirmPassword){
			if(password.length > 5 && password.length <= 15){
				$("#message").html("Password match").css("color", "green");
				$("#submit").prop("disabled", false);
			
			} else {
				$("#message").html("Minimum of 6, max of 15 characters").css("color", "red");
				$("#submit").prop("disabled", true);
			}
			
		} else {
			$("#message").html("Password not match").css("color", "red");
			$("#submit").prop("disabled", true);
		}
	});
}