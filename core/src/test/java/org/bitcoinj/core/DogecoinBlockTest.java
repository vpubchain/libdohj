/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bitcoinj.core;

import org.libdohj.core.AltcoinSerializer;
import java.io.IOException;
import java.math.BigInteger;
import org.libdohj.params.DogecoinMainNetParams;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jrn
 */
public class DogecoinBlockTest {
    private final NetworkParameters params = DogecoinMainNetParams.get();

    @Before
    public void setUp() throws Exception {
        Context context = new Context(params);
    }

    @Test
    public void shouldExtractChainID() {
        final long baseVersion = 2;
        final long flags = 1;
        final long chainID = 98;
        final long auxpowVersion = (chainID << 16) | (flags << 8) | baseVersion;
        assertEquals(chainID, AltcoinBlock.getChainID(auxpowVersion));
    }

    @Test
    public void shouldExtractBaseVersion() {
        final long baseVersion = 2;
        final long flags = 1;
        final long chainID = 98;
        final long auxpowVersion = (chainID << 16) | (flags << 8) | baseVersion;
        assertEquals(baseVersion, AltcoinBlock.getBaseVersion(auxpowVersion));
    }





}
