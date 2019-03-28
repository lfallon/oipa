package com.pennassurancesoftware.oipa.bootstrap;

import org.testng.annotations.Test;

import com.adminserver.asideutilities.globals.CipherUtl;
import com.pennassurancesoftware.oipa.bootstrap.util.Soap;

public class TestConfigureService {

    @Test(groups = { "integration" }, enabled = true)
    public void test() throws Exception {
	final Soap.Default soap = Soap.on(Soap.wsdl("http://oipa:9080/PaletteConfig/ConfigureService?wsdl"),
		"getEnvironments")
		.withCredentials(Soap.credentials("install", CipherUtl.encrypt("install".toCharArray())));

	System.out.println(soap.call());
    }
}
