/**
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.core;

import org.altcoinj.core.AltcoinNetworkParameters;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.List;

import org.altcoinj.core.ScryptHash;
import static org.altcoinj.core.Utils.scryptDigest;

import static org.bitcoinj.core.Utils.reverseBytes;

/**
 * <p>A block is a group of transactions, and is one of the fundamental data structures of the Bitcoin system.
 * It records a set of {@link Transaction}s together with some data that links it into a place in the global block
 * chain, and proves that a difficult calculation was done over its contents. See
 * <a href="http://www.bitcoin.org/bitcoin.pdf">the Bitcoin technical paper</a> for
 * more detail on blocks. <p/>
 *
 * To get a block, you can either build one from the raw bytes you can get from another implementation, or request one
 * specifically using {@link Peer#getBlock(Sha256Hash)}, or grab one from a downloaded {@link BlockChain}.
 */
public class AltcoinBlock extends org.bitcoinj.core.Block {
	/** Bit used to indicate that a block contains an AuxPoW section, where the network supports AuxPoW */
    public static final int BLOCK_VERSION_CHAIN_START = (1 << 16);
    public static final int BLOCK_VERSION_CHAIN_END = (1 << 30);
    public static final int BLOCK_VERSION_AUXPOW = (1 << 8);

    private boolean auxpowParsed = false;
    private boolean auxpowBytesValid = false;

    /** AuxPoW header element, if applicable. */
    @Nullable private AuxPoW auxpow;

    private ScryptHash scryptHash;

    /** Special case constructor, used for the genesis node, cloneAsHeader and unit tests. */
    public AltcoinBlock(NetworkParameters params) {
        super(params);
    }

    /** Special case constructor, used for the genesis node, cloneAsHeader and unit tests. */
    public AltcoinBlock(NetworkParameters params, byte[] payloadBytes) {
        this(params, payloadBytes, params.getDefaultSerializer(), payloadBytes.length);
    }

    /**
     * Construct a block object from the Bitcoin wire format.
     * @param params NetworkParameters object.
     * @param serializer the serializer to use for this message.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    public AltcoinBlock(NetworkParameters params, byte[] payloadBytes, MessageSerializer serializer, int length)
            throws ProtocolException {
        super(params, payloadBytes, serializer, length);
    }

    public AltcoinBlock(NetworkParameters params, byte[] payloadBytes, int offset,
        Message parent, MessageSerializer serializer, int length)
        throws ProtocolException {
        super(params, payloadBytes, serializer, length);
    }

    /**
     * Construct a block initialized with all the given fields.
     * @param params Which network the block is for.
     * @param version This should usually be set to 1 or 2, depending on if the height is in the coinbase input.
     * @param prevBlockHash Reference to previous block in the chain or {@link Sha256Hash#ZERO_HASH} if genesis.
     * @param merkleRoot The root of the merkle tree formed by the transactions.
     * @param time UNIX time when the block was mined.
     * @param difficultyTarget Number which this block hashes lower than.
     * @param nonce Arbitrary number to make the block hash lower than the target.
     * @param transactions List of transactions including the coinbase.
     */
    public AltcoinBlock(NetworkParameters params, long version, Sha256Hash prevBlockHash, Sha256Hash merkleRoot, long time,
                 long difficultyTarget, long nonce, List<Transaction> transactions) {
        super(params, version, prevBlockHash, merkleRoot, time, difficultyTarget, nonce, transactions);
    }

    private ScryptHash calculateScryptHash() {
        try {
            ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(HEADER_SIZE);
            writeHeader(bos);
            return new ScryptHash(reverseBytes(scryptDigest(bos.toByteArray())));
        } catch (IOException e) {
            throw new RuntimeException(e); // Cannot happen.
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e); // Cannot happen.
        }
    }

    public AuxPoW getAuxPoW() {
        // TODO: maybeParseAuxPoW();
        return this.auxpow;
    }

    /**
     * Returns the Scrypt hash of the block (which for a valid, solved block should be
     * below the target). Big endian.
     */
    public ScryptHash getScryptHash() {
        if (scryptHash == null)
            scryptHash = calculateScryptHash();
        return scryptHash;
    }

    /**
     * Returns the Scrypt hash of the block.
     */
    public String getScryptHashAsString() {
        return getScryptHash().toString();
    }

    protected void parseAuxPoW() throws ProtocolException {
        if (this.auxpowParsed)
            return;

        this.auxpow = null;
        if (this.params instanceof AltcoinNetworkParameters) {
            final AltcoinNetworkParameters altcoinParams = (AltcoinNetworkParameters)this.params;
            if (altcoinParams.isAuxPoWBlockVersion(this.getVersion())) {
                // The following is used in dogecoinj, but I don't think we necessarily need it
                // payload.length >= 160) { // We have at least 2 headers in an Aux block. Workaround for StoredBlocks
                this.auxpow = new AuxPoW(params, payload, cursor, this, serializer);
                optimalEncodingMessageSize += auxpow.getOptimalEncodingMessageSize();
            }
        }

        this.auxpowParsed = true;
        this.auxpowBytesValid = serializer.isParseRetainMode();
    }

    @Override
    void parse() throws ProtocolException {
        parseHeader();
        parseAuxPoW();
        parseTransactions();
        length = cursor - offset;
    }

    @Override
    protected void parseTransactions() {
        if (null != this.auxpow) {
            parseTransactions(HEADER_SIZE + auxpow.getMessageSize());
        } else {
            parseTransactions(HEADER_SIZE);
        }
    }

    @Override
    protected void parseLite() throws ProtocolException {
        // Ignore the header since it has fixed length. If length is not provided we will have to
        // invoke a light parse of transactions to calculate the length.
        if (length == UNKNOWN_LENGTH) {
            Preconditions.checkState(serializer.isParseLazyMode(),
                    "Performing lite parse of block transaction as block was initialised from byte array " +
                    "without providing length.  This should never need to happen.");
            parseAuxPoW();
            parseTransactions();
            length = cursor - offset;
        } else {
            transactionBytesValid = !transactionsParsed || serializer.isParseRetainMode() && length > HEADER_SIZE;
        }
        headerBytesValid = !headerParsed || serializer.isParseRetainMode() && length >= HEADER_SIZE;
    }

    @Override
    void writeHeader(OutputStream stream) throws IOException {
        super.writeHeader(stream);
        if (null != this.auxpow) {
            this.auxpow.bitcoinSerialize(stream);
        }
    }

    /** Returns a copy of the block, but without any transactions. */
    @Override
    public Block cloneAsHeader() {
        AltcoinBlock block = new AltcoinBlock(params);
        super.copyBitcoinHeaderTo(block);
        block.auxpow = auxpow;
        return block;
    }

    /** Returns true if the hash of the block is OK (lower than difficulty target). */
    protected boolean checkProofOfWork(boolean throwException) throws VerificationException {
        if (params instanceof AltcoinNetworkParameters) {
            BigInteger target = getDifficultyTargetAsInteger();
        
            final AltcoinNetworkParameters altParams = (AltcoinNetworkParameters)auxpow;
            if (altParams.isAuxPoWBlockVersion(getVersion()) && null != auxpow) {
                return auxpow.checkProofOfWork(this.getHash(), target, throwException);
            }

            BigInteger h = altParams.getBlockDifficultyHash(this).toBigInteger();
            if (h.compareTo(target) > 0) {
                // Proof of work check failed!
                if (throwException)
                    throw new VerificationException("Hash is higher than target: " + getHashAsString() + " vs "
                            + target.toString(16));
                else
                    return false;
            }
            return true;
        } else {
            return super.checkProofOfWork(throwException);
        }
    }

    /**
     * Checks the block data to ensure it follows the rules laid out in the network parameters. Specifically,
     * throws an exception if the proof of work is invalid, or if the timestamp is too far from what it should be.
     * This is <b>not</b> everything that is required for a block to be valid, only what is checkable independent
     * of the chain and without a transaction index.
     *
     * @throws VerificationException
     */
    @Override
    public void verifyHeader() throws VerificationException {
        super.verifyHeader();
    }
}