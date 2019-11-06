package org.libdohj.core;

import org.bitcoinj.core.*;
import org.bitcoinj.core.GetSporksMessage;
import org.bitcoinj.core.GovernanceObject;
import org.bitcoinj.core.GovernanceSyncMessage;
import org.bitcoinj.core.GovernanceVote;
import org.bitcoinj.core.MasternodeBroadcast;
import org.bitcoinj.core.MasternodePaymentVote;
import org.bitcoinj.core.MasternodePing;
import org.bitcoinj.core.MasternodeVerification;
import org.bitcoinj.core.SendCompactBlocksMessage;
import org.bitcoinj.core.SporkMessage;
import org.bitcoinj.core.SyncStatusCount;
import org.bitcoinj.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.bitcoinj.core.Utils.HEX;

/**
 * @author jrn
 */
public class AltcoinSerializer extends BitcoinSerializer {
    private static final Logger log = LoggerFactory.getLogger(AltcoinSerializer.class);
    public AltcoinSerializer(NetworkParameters params, boolean parseRetain) {
        super(params, parseRetain);
    }

    private static final Map<Class<? extends Message>, String> names = new HashMap<Class<? extends Message>, String>();

    static {
        names.put(VersionMessage.class, "version");
        names.put(InventoryMessage.class, "inv");
        names.put(Block.class, "block");
        names.put(GetDataMessage.class, "getdata");
        names.put(Transaction.class, "tx");
        names.put(AddressMessage.class, "addr");
        names.put(Ping.class, "ping");
        names.put(Pong.class, "pong");
        names.put(VersionAck.class, "verack");
        names.put(GetBlocksMessage.class, "getblocks");
        names.put(GetHeadersMessage.class, "getheaders");
        names.put(GetAddrMessage.class, "getaddr");
        names.put(HeadersMessage.class, "headers");
        names.put(BloomFilter.class, "filterload");
        names.put(FilteredBlock.class, "merkleblock");
        names.put(NotFoundMessage.class, "notfound");
        names.put(MemoryPoolMessage.class, "mempool");
        names.put(RejectMessage.class, "reject");
        names.put(GetUTXOsMessage.class, "getutxos");
        names.put(UTXOsMessage.class, "utxos");
        names.put(SendHeadersMessage.class, "sendheaders");
        // SYSCOIN specific
        names.put(SendCompactBlocksMessage.class, "sendcmpct");
        names.put(MasternodeBroadcast.class, "mnb");
        names.put(MasternodePing.class, "mnp");
        names.put(MasternodePaymentVote.class, "mnw");
        names.put(MasternodeVerification.class, "mnv");
        names.put(SporkMessage.class, "spork");
        names.put(GetSporksMessage.class, "getsporks");
        names.put(SyncStatusCount.class, "ssc");
        names.put(GovernanceSyncMessage.class, "govsync");
        names.put(GovernanceObject.class, "govobj");
        names.put(GovernanceVote.class, "govobjvote");
        names.put(MasternodePaymentVote.class, "mnget");
    }

    /**
     * Writes message to to the output stream.
     */
    @Override
    public void serialize(Message message, OutputStream out) throws IOException {
        String name = names.get(message.getClass());
        if (name == null) {
            throw new Error("AltcoinSerializer doesn't currently know how to serialize " + message.getClass());
        }
        super.serialize(name, message.bitcoinSerialize(), out);
    }

    /**
     * Deserialize payload only.  You must provide a header, typically obtained by calling
     * {@link AltcoinSerializer#deserializeHeader}.
     */
    @Override
    public Message deserializePayload(BitcoinSerializer.BitcoinPacketHeader header, ByteBuffer in) throws ProtocolException, BufferUnderflowException {
        byte[] payloadBytes = new byte[header.size];
        in.get(payloadBytes, 0, header.size);

        // Verify the checksum.
        byte[] hash;
        hash = Sha256Hash.hashTwice(payloadBytes);
        if (header.checksum[0] != hash[0] || header.checksum[1] != hash[1] ||
                header.checksum[2] != hash[2] || header.checksum[3] != hash[3]) {
            throw new ProtocolException("Checksum failed to verify, actual " +
                    HEX.encode(hash) +
                    " vs " + HEX.encode(header.checksum));
        }

        if (log.isDebugEnabled()) {
            log.debug("Received {} byte '{}' message: {}", header.size, header.command,
                    HEX.encode(payloadBytes));
        }

        try {
            return makeMessage(header.command, header.size, payloadBytes, hash, header.checksum);
        } catch (Exception e) {
            throw new ProtocolException("Error deserializing message " + HEX.encode(payloadBytes) + "\n", e);
        }
    }

    private Message makeMessage(String command, int length, byte[] payloadBytes, byte[] hash, byte[] checksum) throws ProtocolException {
        // We use an if ladder rather than reflection because reflection is very slow on Android.
        Message message;
        NetworkParameters params = super.getParameters();
        if (command.equals("version")) {
            return new VersionMessage(params, payloadBytes);
        } else if (command.equals("inv")) {
            message = makeInventoryMessage(payloadBytes, length);
        } else if (command.equals("block")) {
            message = makeBlock(payloadBytes, length);
        } else if (command.equals("merkleblock")) {
            message = makeFilteredBlock(payloadBytes);
        } else if (command.equals("getdata")) {
            message = new GetDataMessage(params, payloadBytes, this, length);
        } else if (command.equals("getblocks")) {
            message = new GetBlocksMessage(params, payloadBytes);
        } else if (command.equals("getheaders")) {
            message = new GetHeadersMessage(params, payloadBytes);
        } else if (command.equals("tx")) {
            message = makeTransaction(payloadBytes, 0, length, hash);
        } else if (command.equals("addr")) {
            message = makeAddressMessage(payloadBytes, length);
        } else if (command.equals("ping")) {
            message = new Ping(params, payloadBytes);
        } else if (command.equals("pong")) {
            message = new Pong(params, payloadBytes);
        } else if (command.equals("verack")) {
            return new VersionAck(params, payloadBytes);
        } else if (command.equals("headers")) {
            return new HeadersMessage(params, payloadBytes);
        } else if (command.equals("alert")) {
            return makeAlertMessage(payloadBytes);
        } else if (command.equals("filterload")) {
            return makeBloomFilter(payloadBytes);
        } else if (command.equals("notfound")) {
            return new NotFoundMessage(params, payloadBytes);
        } else if (command.equals("mempool")) {
            return new MemoryPoolMessage();
        } else if (command.equals("reject")) {
            return new RejectMessage(params, payloadBytes);
        } else if (command.equals("utxos")) {
            return new UTXOsMessage(params, payloadBytes);
        } else if (command.equals("getutxos")) {
            return new GetUTXOsMessage(params, payloadBytes);
        } else if (command.equals("sendheaders")) {
            return new SendHeadersMessage(params, payloadBytes);
        } else if (command.equals("mnb")) {
            return new MasternodeBroadcast(params, payloadBytes);
        } else if (command.equals("mnw")){
            return new MasternodePaymentVote(params, payloadBytes);
        } else if( command.equals("mnp")) {
            return new MasternodePing(params, payloadBytes);
        } else if (command.equals("mnv")) {
            return new MasternodeVerification(params, payloadBytes);
        } else if (command.equals("spork")) {
            return new SporkMessage(params, payloadBytes);
        } else if(command.equals("ssc")) {
            return new SyncStatusCount(params, payloadBytes);
        } else if(command.equals("sendcmpct")) {
            return new SendCompactBlocksMessage(params, payloadBytes);
        } else if(command.equals("getsporks")) {
            return new GetSporksMessage(params, payloadBytes);
        } else if(command.equals("govsync")) {
            return new GovernanceSyncMessage(params, payloadBytes);
        } else if(command.equals("govobj")) {
            return new GovernanceObject(params, payloadBytes);
        } else if(command.equals("govobjvote")) {
            return new GovernanceVote(params, payloadBytes);
        } else if(command.equals("mnget")) {
            return new MasternodePaymentVote(params, payloadBytes);
        } else {
            log.warn("No support for deserializing message with name {}", command);
            return new UnknownMessage(params, command, payloadBytes);
        }
        return message;
    }
    @Override
    public Block makeBlock(final byte[] payloadBytes, final int offset, final int length) throws ProtocolException {
        return new AltcoinBlock(getParameters(), payloadBytes, offset, this, length);
    }

    @Override
    public FilteredBlock makeFilteredBlock(byte[] payloadBytes) throws ProtocolException {
        long blockVersion = Utils.readUint32(payloadBytes, 0);
        int headerSize = Block.HEADER_SIZE;

        byte[] headerBytes = new byte[Block.HEADER_SIZE + 1];
        System.arraycopy(payloadBytes, 0, headerBytes, 0, headerSize);
        headerBytes[80] = 0; // Need to provide 0 transactions so the block header can be constructed

        if (this.getParameters() instanceof AuxPoWNetworkParameters) {
            final AuxPoWNetworkParameters auxPoWParams = (AuxPoWNetworkParameters) this.getParameters();
            if (auxPoWParams.isAuxPoWBlockVersion(blockVersion)) {
                final AltcoinBlock header = (AltcoinBlock) makeBlock(headerBytes, 0, Message.UNKNOWN_LENGTH);
                final AuxPoW auxpow = new AuxPoW(this.getParameters(), payloadBytes, Block.HEADER_SIZE, null, this);
                header.setAuxPoW(auxpow);

                int pmtOffset = headerSize + auxpow.getMessageSize();
                int pmtLength = payloadBytes.length - pmtOffset;
                byte[] pmtBytes = new byte[pmtLength];
                System.arraycopy(payloadBytes, pmtOffset, pmtBytes, 0, pmtLength);
                PartialMerkleTree pmt = new PartialMerkleTree(this.getParameters(), pmtBytes, 0);

                return new FilteredBlock(this.getParameters(), header, pmt);
            }
        }

        // We are either not in AuxPoW mode, or the block is not an AuxPoW block.
        return super.makeFilteredBlock(payloadBytes);
    }
}
