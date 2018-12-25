package edu.cmu.ds.task3.server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author ZEXIAN
 */
public class BlockChain {

    List<Block> blocks;
    String chainHash;

    public BlockChain() {        
        blocks = new ArrayList<>();
    }

    /**
     * get current timestamp
     *
     * @return Timestamp
     */
    public static Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    public Block getLatestBlock() {
        if (this.blocks.size() > 0) {
            return this.blocks.get(this.blocks.size() - 1);
        }
        return null;
    }

    /**
     * get chain size
     *
     * @return int
     */
    public int getChainSize() {
        return this.blocks.size();
    }

    /**
     * get hashes compute per second
     *
     * @return int times compute
     */
    public int hashesPerSecond() {
        Timestamp timestamp1 = getTime();
        int count = 0;
        while (getTime().getTime() - timestamp1.getTime() < 1000) {
            HashComputer.ComputeSHA_256_as_Hex_String("00000000");
            count++;
        }
        return count;
    }

    /**
     * add new block to the chain, set chain hash to latest block's hash
     *
     * @param newBlock
     */
    public void addBlock(Block newBlock) {
        newBlock.setIndex(getChainSize());
        newBlock.setPreviousHash(this.chainHash);
        String newHash = newBlock.proofOfWork();
        this.chainHash = newHash;
        this.blocks.add(newBlock);
    }
   /**
    * to String method
    * @return String
    */
    public String toString() {
        try {
//            put into json object and then dumps the json
            JSONObject json = new JSONObject();
            json.put("ds_chain", this.blocks);
            json.put("chainHash", this.chainHash);
            StringWriter writer = new StringWriter();
            json.writeJSONString(writer);
            String jsonString = writer.toString();
            return jsonString;
        } catch (IOException ex) {
            Logger.getLogger(BlockChain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * valid a chain
     *
     * @return boolean
     */
    public boolean isChainValid() {
        if (getChainSize() == 0) {
            return true;
        }
        //valid first block
        Block blockOne = this.blocks.get(0);
        if (!isBlockValid(blockOne)) {
            System.out.println("Improper hash on node one Does not begin with " + blockOne.getDifficulty() + " 0s");
            return false;
        }
        // valid each block
        for (int i = 1; i < getChainSize(); i++) {
            if (!isBlockValid(blocks.get(i))) {
                System.out.println("Improper hash on node " + i + " Does not begin with " + blocks.get(i).getDifficulty() + " 0s");
                return false;
            }
            // valid if current block's getPreviousHash is previous block's hash
            if (!blocks.get(i).getPreviousHash().equals(blocks.get(i - 1).calculateHash())) {
                System.out.println("Improper preivous hash on node " + i + " Does not equal to " + i + "th block's hash");
                return false;
            }
        }
        //valid if chainhash is the latest block's hash
        if (!this.chainHash.equals(blocks.get(getChainSize() - 1).calculateHash())) {
            System.out.println("The chain hash is not equal to last block's hash");
            return false;
        }
        return true;
    }
    
    /**
     * repair the block chain
     */
    public void repairChain() {
        // if is valid, do nothing
        if (isChainValid()) {
            return;
        }
        //get hash of first block and also repair it
        String prevHash = this.blocks.get(0).proofOfWork();
        // repair each of block and set prev hash to it
        for (int i = 1; i < this.getChainSize(); i++) {
            this.blocks.get(i).setPreviousHash(prevHash);
            prevHash = this.blocks.get(i).proofOfWork();
        }
        // set chain hash to latest block's hash
        this.chainHash = prevHash;
    }

    private boolean isBlockValid(Block block) {

        if (countLeadingZero(block.calculateHash()) < block.getDifficulty()) {
            return false;
        }
        return true;
    }

    /**
     * function to get leading zero of hash
     *
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
//    //int index, 
//                 Timestamp timestamp, 
//                 String data, 
//                 int difficulty

    public static void main(String[] args) {
        BlockChain bc = new BlockChain();
        Block Genesis = new Block(0, getTime(), "Genesis", 2);
        bc.addBlock(Genesis);
        Scanner keyboard = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\n0. View basic blockchain status.\n"
                        + "\n"
                        + "1. Add a transaction to the blockchain.\n"
                        + "\n"
                        + "2. Verify the blockchain.\n"
                        + "\n"
                        + "3. View the blockchain.\n"
                        + "\n"
                        + "4. Corrupt the chain.\n"
                        + "\n"
                        + "5. Hide the corruption by repairing the chain.\n"
                        + "\n"
                        + "6. Exit.\n");
                int option = keyboard.nextInt();
                keyboard.nextLine();
                if (option < 0 || option > 6) {
                    System.out.println("invalid option, input again");
                    continue;
                }
                switch (option) {
                    case 0:
                        System.out.println("Current size of chain: " + bc.getChainSize());
                        System.out.println("Current hashes per second by this machine: " + bc.hashesPerSecond());
                        break;
//                  The program would have a longer delay when I create a block with a difficulty higher than 4
//                  On average, it takes about 700 milliseconds to add a block whose difficulty is 4 and about
//                  3000 milliseconds to add a block whose difficulty is 5
//                  Sometime, the speed maybe higher and sometime the speed is lower, I think it may depends on the 
//                  timestamp and computer performance.
                    case 1:
                        System.out.println("Enter difficulty > 0");
                        int dif = keyboard.nextInt();
                        keyboard.nextLine();
                        if (dif < 0) {
                            System.out.println("difficulty should larger than 0");
                            continue;
                        }
                        System.out.println("Enter transaction");
                        String data = keyboard.nextLine();
                        Block newBlock = new Block(bc.getChainSize(), getTime(), data, dif);
                        Timestamp timestamp1 = getTime();
                        bc.addBlock(newBlock);
                        Timestamp timestamp2 = getTime();
                        System.out.println("Total execution time to add this block was "
                                + (timestamp2.getTime() - timestamp1.getTime()) + " milliseconds");
                        break;
//                        It takes just about 1 millisecond to verify a block
                    case 2:
                        System.out.println("Verifying entire chain");
                        Timestamp timestamp5 = getTime();
                        System.out.println("Chain verification: " + bc.isChainValid());
                        Timestamp timestamp6 = getTime();
                        System.out.println("Total execution time to verify the chain is "
                                + (timestamp6.getTime() - timestamp5.getTime()) + " milliseconds");
                        break;
                    case 3:
                        System.out.println("View the blockChain");
                        System.out.println(bc.toString());
                        break;
//                        change the data of block
                    case 4:
                        System.out.println("Corrupt the Blockchain\nEnter block ID of block to corrupt (less than " + bc.getChainSize() + ")");
                        int blockNum = keyboard.nextInt();
                        keyboard.nextLine();
                        if (blockNum < 0 || blockNum >= bc.getChainSize()) {
                            System.out.println("invalid input");
                            continue;
                        }
                        System.out.println("Enter new data for block " + blockNum);
                        String newData = keyboard.nextLine();
                        bc.blocks.get(blockNum).setData(newData);
                        System.out.println("Block " + blockNum + " now holds " + newData);
                        break;
//                        It takes more than 150000 milliseconds to repair a chain with length larger than 3. The delay is really significant
//                        especially when there is a block with more than 4 difficulty in the blockchain
                    case 5:
                        System.out.println("Repairing the entire chain");
                        Timestamp timestamp3 = getTime();
                        bc.repairChain();
                        System.out.println("Total execution time required to repair the chain was " + (getTime().getTime() - timestamp3.getTime()) + " milliseconds");
                        break;
                    case 6:
                        return;
                }
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("input again");
                continue;
            }
        }
    }
}
