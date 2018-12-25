/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ds.task3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ZEXIAN
 */
@WebServlet(name = "BlockChainServer", urlPatterns = {"/BlockChainServer"})
public class BlockChainServer extends HttpServlet {
    BlockChain chain;
    BigInteger e = new BigInteger("65537");
    BigInteger n = new BigInteger("2688520255179015026237478731436571621031218154515572968727588377065598663770912513333018006654248650656250913110874836607777966867106290192618336660849980956399732967369976281500270286450313199586861977623503348237855579434471251977653662553");
    
    @Override
    public void init() {
        chain = new BlockChain();
        Block block = new Block(0, BlockChain.getTime(), "Genesis", 2);
        this.chain.addBlock(block);
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * use get to get verify and view the blockchain
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = request.getParameter("type");
//        handle invalid request
        if (type == null || type.length() == 0) {
            response.setStatus(400);
            PrintWriter out = response.getWriter();
            out.println("wrong request");
            return;
        } 
//        if type is verify, return verify result
        if (type.equals("verify")) {
            response.setStatus(200);
            response.setContentType("text/plain;charset=UTF-8");
            boolean result = VerifyBlockchain();
            PrintWriter out = response.getWriter();
            out.println(result);
//            if type is view, return chain 
        } else if (type.equals("view")) {
            response.setStatus(200);
            response.setContentType("text/plain;charset=UTF-8");
            String view = ViewBlockchain();
            PrintWriter out = response.getWriter();
            out.println(view);
        } else {
//            if other type, invalid
            response.setStatus(400);
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("wrong request");
        }
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     * use put method because it is used to create block and it is idempotent
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String data = br.readLine();
//        use regex to get the data
        Pattern p = Pattern.compile("difficulty=([0-9]+)&data=(.*)");
	Matcher matcher = p.matcher(data);
//        handle invalid request
        if (!matcher.find()) {
            response.setStatus(400);
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("Invalid post request");
            return;
        }
//        get diff and data
        String diff = matcher.group(1);
        String d = matcher.group(2);
//        create block
        String res = AddTransaction(Integer.parseInt(diff), d);
//        send 200 response
        response.setStatus(200);
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(res);
    }
        /**
         * create block
         * @param difficulty int
         * @param data String
         * @return String
         */
        public String AddTransaction(int difficulty, String data) {
        try {
//            verify the input first, if hashed value is same, they success, or return fail and do nothing
            System.out.println(data);           
            String[] strs = data.split("#");
            if (!verify(strs[0], strs[1])) {
                return "Transaction Fail";
            }
            System.out.println(data);
//            create a block, the index is set the size of chain which means the index in the chain
            Block block = new Block(this.chain.getChainSize(), BlockChain.getTime(), data, difficulty);
            Timestamp timestamp1 = BlockChain.getTime();
//            add block to the chain
            this.chain.addBlock(block);
            long timeSpent = BlockChain.getTime().getTime() - timestamp1.getTime();
            return "Total execution time to add this block " + timeSpent + " milliseconds";
        } catch (Exception ex) {
            Logger.getLogger(BlockChainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "System error";
    }
    /**
     * return if the chain is valid
     * @return boolean
     */
    public Boolean VerifyBlockchain() {
        return this.chain.isChainValid();
    }
    /**
     * view the block
     * @return String
     */
    public String ViewBlockchain() {
        return this.chain.toString();
    }
    
    /**
     * Verifying proceeds as follows:
     * 1) Decrypt the encryptedHash to compute a decryptedHash
     * 2) Hash the messageToCheck using SHA-256 (be sure to handle 
     *    the extra byte as described in the signing method.)
     * 3) If this new hash is equal to the decryptedHash, return true else false.
     * 
     * @param messageToCheck  a normal string that needs to be verified.
     * @param encryptedHashStr integer string - possible evidence attesting to its origin.
     * @return true or false depending on whether the verification was a success
     * @throws Exception 
     */
    public boolean verify(String messageToCheck, String encryptedHashStr)throws Exception  {
        
        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(encryptedHashStr);
        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);
        
        // Get the bytes from messageToCheck
        byte[] bytesOfMessageToCheck = messageToCheck.getBytes("UTF-8");
       
        // compute the digest of the message with SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        
        byte[] messageToCheckDigest = md.digest(bytesOfMessageToCheck);
        
        // messageToCheckDigest is a full SHA-256 digest
        // take two bytes from SHA-256 and add a zero byte
        byte[] extraByte = new byte[messageToCheckDigest.length + 1];
        extraByte[0] = 0;
        for (int i = 0; i < messageToCheckDigest.length; i++) {
            extraByte[i + 1] = messageToCheckDigest[i];
        }
        
        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(extraByte);
        
        // inform the client on how the two compare
        if(bigIntegerToCheck.compareTo(decryptedHash) == 0) {
            return true;
        }
        else {
            return false;
        }
    }

}
