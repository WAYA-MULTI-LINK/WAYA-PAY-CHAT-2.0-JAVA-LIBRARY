package com.faymosInc.wayapaylib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.faymosInc.wayapaylib.services.PaymentTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WayapaylibApplication {

	public static void main(String[] args) throws JsonProcessingException {
		SpringApplication.run(WayapaylibApplication.class, args);
//
//		PaymentTransaction fff = new PaymentTransaction();
//		var hh= fff.queryPayment("1653341407543518988","test");
//				var ggg = hh;
////		PaymentTransaction ff = new PaymentTransaction();
//		var  ss = ff.initiatePayment("128.00","Order from Luke Vincent","Luke Vincent","wakexow@mailinator.com",
//				"11948667447","MER_qZaVZ1645265780823HOaZW","WAYAPUBK_TEST_0x3442f06c8fa6454e90c5b1a518758c70","test");
//
//		var  dd = ss;
	}

}
