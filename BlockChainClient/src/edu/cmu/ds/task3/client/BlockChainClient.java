/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ds.task3.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZEXIAN
 */
public class BlockChainClient {
    BigInteger d = new BigInteger("339177647280468990599683753475404338964037287357290649639740920420195763493261892674937712727426153831055473238029100340967145378283022484846784794546119352371446685199413453480215164979267671668216248690393620864946715883011485526549108913");
    BigInteger n = new BigInteger("2688520255179015026237478731436571621031218154515572968727588377065598663770912513333018006654248650656250913110874836607777966867106290192618336660849980956399732967369976281500270286450313199586861977623503348237855579434471251977653662553");
  
    public static void main(String[] args) throws Exception{
        BlockChainClient bc = new BlockChainClient();
        bc.run();
    }
    
    private void run() {
        Scanner keyboard = new Scanner(System.in);
        while (true) {
            try {
                //print the instruction
                System.out.println("1. Add a transaction to the blockchain.\n"
                        + "2. Verify the blockchain.\n"
                        + "3. View the blockchain.\n"
                        + "4. Exit\n");
                int option = keyboard.nextInt();
                keyboard.nextLine();
                // see if option is valid
                if (option <= 0 || option > 4) {
                    System.out.println("invalid option, input again");
                    continue;
                }
                String request = "";
                switch (option) {
//                    create block
                    case 1:
                        System.out.println("Enter difficulty > 0");
                        int dif = keyboard.nextInt();
                        keyboard.nextLine();
                        // see if difficulty is valid
                        if (dif < 0) {
                            System.out.println("difficulty should larger than 0");
                            continue;
                        }
                        System.out.println("Enter transaction");
                        String data = keyboard.nextLine();
                        // call remote addtransaction function
                        putRequest(dif, data + "#" + signStr(data));
                        break;
                    case 2:
                        // call remote verify function
                        System.out.println("Verifying entire chain");
                        System.out.print("Chain verification: ");
                        getRequest("verify");
                        break;                    
                    case 3:
                        getRequest("view");
                        break;
                    case 4:
                        return;
                }
            } catch (java.lang.Exception e) {
                System.out.println(e);
                System.out.println("input again");
                continue;
            }
        }
    }
    /**
     * create a block with put request
     * @param diff int
     * @param data String
     */
    public void putRequest(int diff, String data) {
        try {
            URL url = new URL("http://localhost:8080/Project3Task3Server/BlockChainServer");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
//          cite from https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
            String urlParameters = "difficulty=" + diff + "&data=" + data;
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//            write the data
            wr.writeBytes(urlParameters);
            wr.flush();
	    wr.close();
            int responseCode = con.getResponseCode();
//            if 200, fail
            if (responseCode != 200) {
                System.out.println("Fail to create transaction!");
                return;
            }
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
//            get response info
            while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(BlockChainClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BlockChainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void getRequest(String type) {
        try {
            String url = "http://localhost:8080/Project3Task3Server/BlockChainServer?type=" + type;
//            cite from https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestProperty("Accept", "text/xml");
            // optional default is GET
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Get error");
                return;
            }
            System.out.println("Response Code : " + responseCode);
//            read response
            BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            
            while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
                
		//print result
		System.out.println(response.toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(BlockChainClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(BlockChainClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BlockChainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * sign the string
     * @param str
     * @return 
     */
    private String signStr(String str) {
        try {
            // compute the digest with SHA-256
            byte[] bytesOfMessage = str.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bigDigest = md.digest(bytesOfMessage);
            
            // we only want two bytes of the hash for BabySign
            // we add a 0 byte as the most significant byte to keep
            // the value to be signed non-negative.
            byte[] messageDigest = new byte[bigDigest.length + 1];
            messageDigest[0] = 0;   // most significant set to 0
            for (int i = 0; i < bigDigest.length; i++) {
                messageDigest[i + 1] = bigDigest[i];
            }
            // The message digest now has three bytes. Two from SHA-256
            // and one is 0.
            
            // From the digest, create a BigInteger
            BigInteger m = new BigInteger(messageDigest);
            
            // encrypt the digest with the private key
            BigInteger c = m.modPow(d, n);
            
            // return this as a big integer string
            return c.toString();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BlockChainClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
}
