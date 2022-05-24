# WAYA-PAY-CHAT-2.0-JAVA-LIBRARY

This is a JAVA library for implementing WayaPay payment gateway

#Getting Started
This JAVA library provides a wrapper to implement WayaPay Payment to your application

#Usage
This library can be implemented into your application by importing it.
Download wayapaylib.zip from https://github.com/WAYA-MULTI-LINK/WAYA-PAY-CHAT-2.0-JAVA-LIBRARY/raw/Faymos-patch-1/wayapaylib.zip  and Extract the .jar file and copy it to your project folder

Add jar file as a Module to your Java project:

      On Intellij IDEA: File -> Project Structure -> libraries -> Click + sign then select java ->select the wayapaylib.jar from the Directories you copy it to-> click ok and apply
      
      On Netbeans: Project properties -> Libraries -> Compile -> ADD JAR/folder -> Add Jar
      
#For Initialize payment Method

 PaymentTransaction paymentTransaction = new PaymentTransaction();
 ResponseData ss = tt.initiatePayment("amount","description","name","email","phoneNumber","merchantId","wayaPublicKey","mode");
 this method returned status of the intialize payment, transid and Authorization URL(authurl) which you will redirect your user to for payment.

#For Query/Verify payment Method
PaymentTransaction paymentTransaction = new PaymentTransaction();
JSONObject ss =  paymentTransaction.queryPayment("transId","mode");
 
 #NOTE for test purpose use test as mode and use live for production


#SAMPLE TEST INITIALIZE REQUEST

    PaymentTransaction paymentTransaction = new PaymentTransaction();
  	ResponseData  response = paymentTransaction.initiatePayment("128.00","Order from Luke Vincent","Luke Vincent","wakexow@mailinator.com",
		"11948667447","MER_qZaVZ1645265780823HOaZW","WAYAPUBK_TEST_0x3442f06c8fa6454e90c5b1a518758c70","test");
		
    
#SAMPLE TEST INITIALIZE RESPONSE

{
    "message": "Success Transaction",
    "transid":"1653395156669782335"
    "authUrl": "https://pay.staging.wayapay.ng/?_tranId=1653395156669782335",
    "status": true
}


#SAMPLE TEST VERIFY REQUEST


        JSONObject ss =  paymentTransaction.queryPayment("1653341407543518988","test");
 
 
#SAMPLE TEST INITIALIZE RESPONSE
{
    "timeStamp": 1653379174758,
    "status": true,
    "message": "Transaction Query",
    "data": {
        "Amount": 128.00,
        "Description": "Order from Luke Vincent",
        "Fee": 1.00,
        "Currency": "566",
        "Status": "PENDING",
        "productName": "CARD",
        "businessName": "Spicy Beauty",
        "customer": {
            "name": "Luke Vincent",
            "email": "wakexow@mailinator.com",
            "phoneNumber": "+11948667447",
            "customerId": "CUS_gFtUQ16486350598356yz97"
        }
    }
}
