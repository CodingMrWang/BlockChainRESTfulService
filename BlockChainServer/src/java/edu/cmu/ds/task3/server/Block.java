/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ds.task3.server;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

/**
 *
 * @author ZEXIAN
 */
public class Block {
    
    private int index;
    private Timestamp timestamp;
    private String data;
    private String previousHash;
    private BigInteger nonce;
    private int difficulty;
    
           
    /**
     * 
     * @param index the position of the block on the chain. The first block (the so called Genesis block) has an index of 0
     * @param timestamp  a Java Timestamp object, it holds the time of the block's creation
     * @param data a String holding the block's single transaction details.
     * @param difficulty This is the number of leftmost nibbles that need to be 0.
     */
    public Block(int index, 
                 Timestamp timestamp, 
                 String data, 
                 int difficulty) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = new BigInteger(String.valueOf(0));
    }
    /**
     * This method computes a hash of the concatenation of the index, timestamp, data, previousHash, nonce, and difficulty.
     * @return a String holding Hexadecimal characters
     */
    public String calculateHash() {
        return HashComputer.ComputeSHA_256_as_Hex_String(String.valueOf(this.index) + 
                this.timestamp.toString() + this.data + this.previousHash 
                + this.nonce.toString() + String.valueOf(difficulty));
    }
    /**
     * The proof of work methods finds a good hash. It increments the nonce until it produces a good hash.
     * @return a String with a hash that has the appropriate number of leading hex zeroes
     */
    public String proofOfWork() {
        while (countLeadingZero(calculateHash()) < this.difficulty) {
            this.nonce = this.nonce.add(new BigInteger(String.valueOf(1)));
        }
        return calculateHash();
    }
    
    /**
     * getter to get difficulty
     * @return int difficulty
     */
    public int getDifficulty() {
        return this.difficulty;
    }
    
    /**
     * setter of difficulty
     * @param difficulty int
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    /**
     * toString 
     * @return return json string of data
     */
    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put("index", this.index);
            json.put("time stamp ", this.timestamp);
            json.put("Tx ", this.data);
            json.put("PrevHash", this.previousHash);
            json.put("nonce", this.nonce);
            json.put("difficulty", this.difficulty);
            StringWriter writer = new StringWriter();
            json.writeJSONString(writer);
            String jsonString = writer.toString();
            return jsonString;
        } catch (IOException ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    /**
     * setter of previousHash
     * @param previousHash String
     */
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    
    /**
     * getter of PreviousHash
     * @return String
     */
    public String getPreviousHash() {
        return this.previousHash;
    }

    /**
     * getter of index
     * @return int 
     */
    public int getIndex() {
        return this.index;
    }
    
    /**
     * setter of index
     * @param index int
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * setter of timestamp
     * @param timestamp Timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    /**
     * getter of timestamp
     * @return 
     */
    public Timestamp getTimestamp() {
        return this.timestamp;
    }
    
    /**
     * getter of data
     * @return String
     */
    public String getData() {
        return this.data;
    }
    /**
     * setter of data
     * @param data String
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * function to get leading zero of hash
     * @param hash String 
     * @return int num of leading zero
     */
    private int countLeadingZero(String hash) {
        int count = 0;
        for (int i = 0; i < hash.length(); i++) {
            if (hash.charAt(i) == '0') {
                count++;
            } else {
                return count;
            }
        }
        return count;
    }
    
    public static void main(java.lang.String[] args) {
        Block b = new Block(0, new Timestamp(2018, 10, 8, 20, 30, 30, 100), "hello world", 2);
        System.out.println(b.proofOfWork());
    }
}
