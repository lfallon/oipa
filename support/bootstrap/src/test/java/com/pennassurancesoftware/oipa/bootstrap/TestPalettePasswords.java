package com.pennassurancesoftware.oipa.bootstrap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.adminserver.asideutilities.globals.CipherUtl;

public class TestPalettePasswords {

    @Test(groups = { "unit" }, enabled = true)
    public void pw1() throws Exception {
	final String encrypted = "V:!\"[K#aC2P/NB";
	final String decrypted = "SQLServerPass1";

	Assert.assertEquals(CipherUtl.encrypt(decrypted.toCharArray()), encrypted);
	Assert.assertEquals(CipherUtl.decrypt(encrypted.toCharArray()), decrypted);
    }
}
