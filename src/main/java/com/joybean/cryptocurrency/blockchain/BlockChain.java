package com.joybean.cryptocurrency.blockchain;

import java.util.HashMap;
import java.util.Map;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.
public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private Map<ByteArrayWrapper, Node> map;
    private TransactionPool transactionPool;
    private Node maxHeightNode;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        map = new HashMap<>();
        UTXOPool utxoPool = new UTXOPool();
        addBlockToUTXOPool(utxoPool, genesisBlock);
        Node root = new Node(genesisBlock, 0, utxoPool);
        putBlockNode(root);
        map.put(new ByteArrayWrapper(genesisBlock.getHash()), root);
        maxHeightNode = root;
        transactionPool = new TransactionPool();
    }

    private void addBlockToUTXOPool(UTXOPool utxoPool, Block block) {
        Transaction transaction = block.getCoinbase();
        utxoPool.addUTXO(new UTXO(transaction.getHash(), 0), transaction.getOutput(0));
    }

    /**
     * Get the maximum height block
     */
    public Block getMaxHeightBlock() {
        return maxHeightNode.getBlock();
    }

    /**
     * Get the UTXOPool for mining a new block on top of max height block
     */
    public UTXOPool getMaxHeightUTXOPool() {
        return maxHeightNode.getUtxoPool();
    }

    /**
     * Get the transaction pool to mine a new block
     */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * <p>
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        //genesis block
        if (block.getPrevBlockHash() == null) {
            return false;
        }
        Node preNode = getNode(block.getPrevBlockHash());
        if (preNode == null) {
            return false;
        }
        int newHeight = preNode.getHeight() + 1;
        if (newHeight <= maxHeightNode.getHeight() - CUT_OFF_AGE) {
            return false;
        }

        TxHandler txHandler = new TxHandler(preNode.getUtxoPool());
        Transaction[] validTxs = txHandler.handleTxs(block.getTransactions().toArray(new Transaction[0]));
        if (validTxs.length != block.getTransactions().size()) {
            return false;
        }

        addBlockToUTXOPool(txHandler.getUTXOPool(), block);

        int maxHeight = maxHeightNode.getHeight();
        Node newNode = new Node(block, newHeight, txHandler.getUTXOPool());
        if (newHeight > maxHeight) {
            maxHeightNode = newNode;
        }
        putBlockNode(newNode);
        return true;
    }

    /**
     * Add a transaction to the transaction pool
     */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }

    private Node getNode(final byte[] hash) {
        if (hash == null) {
            return null;
        }
        return this.map.get(new ByteArrayWrapper(hash));
    }

    private void putBlockNode(final Node node) {
        this.map.put(new ByteArrayWrapper(node.getBlock().getHash()), node);
    }

    class Node {
        private Block block;
        private int height;
        private UTXOPool utxoPool;

        public Node(Block block, int height, UTXOPool utxoPool) {
            this.block = block;
            this.height = height;
            this.utxoPool = utxoPool;
        }

        public Block getBlock() {
            return block;
        }

        public void setBlock(Block block) {
            this.block = block;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public UTXOPool getUtxoPool() {
            return new UTXOPool(utxoPool);
        }

        public void setUtxoPool(UTXOPool utxoPool) {
            this.utxoPool = utxoPool;
        }
    }
}