/*
 * Copyright 2013 Google Inc.
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

package org.libdohj.params;

import org.bitcoinj.core.Utils;

import static com.google.common.base.Preconditions.checkState;
/**
 * Parameters for the Syscoin testnet, a separate public network that has
 * relaxed rules suitable for development and testing of applications and new
 * Syscoin versions.
 */
public class SyscoinTestNet3Params extends AbstractSyscoinParams {
    public static final int TESTNET_MAJORITY_WINDOW = 1000;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 750;
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 501;

    public SyscoinTestNet3Params() {
        super();
        id = ID_SYSCOIN_TESTNET;

        packetMagic = 0xcee2cafe;
        maxTarget = Utils.decodeCompactBits(0x1e0ffff0);
        port = 18369;
        addressHeader = 65;
        p2shHeader = 196;
        dumpedPrivateKeyHeader = 239;
        segwitAddressHrp = "tsys";
        genesisBlock.setTime(1553041506L);
        genesisBlock.setDifficultyTarget(0x1e0ffff0);
        genesisBlock.setNonce(1018586);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("000007f96fcbdbdfbc2560b63bb545648f8d9f27c15ae8f5bbc350218198704e"));


        majorityEnforceBlockUpgrade = TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = TESTNET_MAJORITY_WINDOW;

       /* dnsSeeds = new String[] {
            "testseed.jrn.me.uk"
        };*/


        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
        bip32HeaderP2WPKHpub = 0x04b24746; // The 4 byte header that serializes in base58 to "zpub".
        bip32HeaderP2WPKHpriv = 0x04b2430c; // The 4 byte header that serializes in base58 to "zprv"
    }

    private static SyscoinTestNet3Params instance;
    public static synchronized SyscoinTestNet3Params get() {
        if (instance == null) {
            instance = new SyscoinTestNet3Params();
        }
        return instance;
    }

    @Override
    public boolean allowMinDifficultyBlocks() {
        return true;
    }

    @Override
    public String getPaymentProtocolId() {
        // TODO: CHANGE ME
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }

    @Override
    public boolean isTestNet() {
        return true;
    }
}
