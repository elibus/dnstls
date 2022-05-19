package org.freaknet.elibus.dnstls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

@SpringBootTest(
    properties = { "host=1.1.1.1", "port=8553" }
)
class ApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true);
	}

	// These tests should be improved to work with a mock DNS server.
	@Test
	void shouldResolveWithUdp() throws IOException, InterruptedException {

		Record expected = queryDns("example.com.", "1.1.1.1", 53, false);
		Record actual = queryDns("example.com.", "127.0.0.1", 8553, false);

		assertThat(actual, equalTo(expected));
	}

	@Test
	void shouldResolveWithTcp() throws IOException, InterruptedException {

		Record expected = queryDns("example.com.", "1.1.1.1", 53, true);
		Record actual = queryDns("example.com.", "127.0.0.1", 8553, true);

		assertThat(actual, equalTo(expected));
	}

	private static Record queryDns(String query, String host, int port, boolean tcpFlag) throws IOException, InterruptedException {

		Record queryRecord = Record.newRecord(Name.fromString(query), Type.A, DClass.IN);
		Message queryMessage = Message.newQuery(queryRecord);
		Resolver r = new SimpleResolver(host);
		r.setPort(port);
		r.setTimeout(2);
		r.setTCP(tcpFlag);

		return r.send(queryMessage).getSectionArray(Section.ANSWER)[0];
	}
}
