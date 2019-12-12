package com.zam.logviewer.renderers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FIXRendererTest
{
    private final FIXRenderer fixRenderer = new FIXRenderer();

    @Test
    void shouldntRenderInvalidFix()
    {
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.4.4|9=196|35=X||49=A|10=171|");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldntRenderInvalidFixKey()
    {
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.4.4|9=196|35=X|200023=A|49=A|10=171|");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--200023[200023] = A");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMsgWithPipeDelimiter()
    {
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.4.4|9=196|35=X|49=A|56=B|34=12|52=20100318-03:21:11.364|262=A|268=2|279=0|269=0|" +
                        "278=BID|55=EUR/USD|270=1.37215|15=EUR|271=2500000|346=1|279=0|269=1|278=OFFER|55=EUR/USD|" +
                        "270=1.37224|15=EUR|271=2503200|346=1|10=171|");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("  |--MDEntryID[278] = OFFER");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37224");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2503200");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderUnknownFieldsWithoutBreakingStructure()
    {
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.4.4|9=196|35=X|49=A|56=B|34=12|52=20100318-03:21:11.364|262=A|268=2|279=0|269=0|" +
                        "278=BID|55=EUR/USD|270=1.37215|15=EUR|271=2500000|346=1|279=0|269=1|278=OFFER|55=EUR/USD|" +
                        "270=1.37224|15=EUR|201120=UNKNOWN_FIELD_1|201121=UNKNOWN_FIELD_2|271=2503200|346=1|10=171|");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("  |--MDEntryID[278] = OFFER");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37224");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--201120[201120] = UNKNOWN_FIELD_1");
        expected.add("  |--201121[201121] = UNKNOWN_FIELD_2");
        expected.add("  |--MDEntrySize[271] = 2503200");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderBadGroups()
    {
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.4.4|9=196|35=X|49=A|56=B|34=12|52=20100318-03:21:11.364|262=A|268=2|279=0|269=0|" +
                        "278=BID|55=EUR/USD|270=1.37215|15=EUR|271=2500000|346=1|279=0|269=1|11=FakeId|12=FakeNumber|" +
                        "346=1|10=171|");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("*--ClOrdID[11] = FakeId");
        expected.add("*--Commission[12] = FakeNumber");
        expected.add("*--NumberOfOrders[346] = 1");
        expected.add("*--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMsgWithNestedGroups()
    {
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.4.4|9=196|35=X|49=A|56=B|34=12|52=20100318-03:21:11.364|262=A|268=2|279=0|269=0|" +
                        "278=BID|55=EUR/USD|270=1.37215|15=EUR|271=2500000|711=2|311=SYM1|312=SFX1|311=SYM2|312=SFX2|" +
                        "346=1|279=0|269=1|278=OFFER|55=EUR/USD|270=1.37224|15=EUR|271=2503200|346=1|10=171|");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NoUnderlyings[711] = 2");
        expected.add("    +--UnderlyingSymbol[311] = SYM1");
        expected.add("    |--UnderlyingSymbolSfx[312] = SFX1");
        expected.add("    +--UnderlyingSymbol[311] = SYM2");
        expected.add("    |--UnderlyingSymbolSfx[312] = SFX2");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("  |--MDEntryID[278] = OFFER");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37224");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2503200");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMsgWithSOHDelimiter()
    {
        final List<String> actual = fixRenderer.renderBottomPaneContents(
                "8=FIX.4.4\u00019=196\u000135=X\u000149=A\u000156=B\u000134=12\u000152=20100318-03:21:11.364" +
                "\u0001262=A\u0001268=2\u0001279=0\u0001269=0\u0001278=BID\u000155=EUR/USD\u0001270=1.37215\u0001" +
                "15=EUR\u0001271=2500000\u0001346=1\u0001279=0\u0001269=1\u0001278=OFFER\u000155=EUR/USD\u0001" +
                "270=1.37224\u000115=EUR\u0001271=2503200\u0001346=1\u000110=171\u0001");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("  |--MDEntryID[278] = OFFER");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37224");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2503200");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMsgInLogLine()
    {

        final List<String> actual = fixRenderer.renderBottomPaneContents(
                "20171016-12:44:21.456 [thread-1] INFO com.zam.logviewer.Dummy - 8=FIX.4.4\u00019=196\u000135=X" +
                "\u000149=A\u000156=B\u000134=12\u000152=20100318-03:21:11.364\u0001262=A\u0001268=2\u0001279=0" +
                "\u0001269=0\u0001278=BID\u000155=EUR/USD\u0001270=1.37215\u000115=EUR\u0001271=2500000\u0001346=1" +
                "\u0001279=0\u0001269=1\u0001278=OFFER\u000155=EUR/USD\u0001270=1.37224\u000115=EUR\u0001271=2503200" +
                "\u0001346=1\u000110=171\u0001 <- received this message");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("  |--MDEntryID[278] = OFFER");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37224");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2503200");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMsgWithMultilineText()
    {
        final List<String> actual = fixRenderer.renderBottomPaneContents(
                "20171016-12:44:21.456 [thread-1] INFO com.zam.logviewer.Dummy - 8=FIX.4.4|9=196|35=X" +
                "|49=A|56=B|34=12|52=20100318-03:21:11.364|262=A|268=1|279=0" +
                "|269=0|278=BID|55=EUR/USD|58=Weird\n" +
                "(but perfectly valid)\n" +
                "multiline text\n" +
                "which also contains '=' character" +
                "|270=1.37215|15=EUR|271=2500000|346=1" +
                "|10=171| <- received this message");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--Text[58] = Weird");
        expected.add("(but perfectly valid)");
        expected.add("multiline text");
        expected.add("which also contains '=' character");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("|--CheckSum[10] = 171");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMessageTruncatedInTheMiddleOfLastValue()
    {
        final List<String> actual = fixRenderer.renderBottomPaneContents(
                "20171016-12:44:21.456 [thread-1] INFO com.zam.logviewer.Dummy - 8=FIX.4.4|9=196|35=X" +
                        "|49=A|56=B|34=12|52=20100318-03:21:11.364|262=A|268=2|279=0" +
                        "|269=0|278=BID|55=EUR/USD|270=1.37215|15=EUR|271=2500000|346=1" +
                        "|279=0|269=1|278=OFFER|55=EUR/USD|270=1.37224|15=EUR|271=250");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("  |--MDEntryID[278] = OFFER");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37224");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 250");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMessageTruncatedInTheMiddleOfLastTag()
    {
        final List<String> actual = fixRenderer.renderBottomPaneContents(
                "20171016-12:44:21.456 [thread-1] INFO com.zam.logviewer.Dummy - 8=FIX.4.4|9=196|35=X" +
                        "|49=A|56=B|34=12|52=20100318-03:21:11.364|262=A|268=2|279=0" +
                        "|269=0|278=BID|55=EUR/USD|270=1.37215|15=EUR|271=2500000|346=1" +
                        "|279=0|269=1|278=OFFER|55=EUR/USD|270=1.37224|15=EUR|27");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.4.4");
        expected.add("|--BodyLength[9] = 196");
        expected.add("|--MsgType[35] = MARKET_DATA_INCREMENTAL_REFRESH[X]");
        expected.add("|--SenderCompID[49] = A");
        expected.add("|--TargetCompID[56] = B");
        expected.add("|--MsgSeqNum[34] = 12");
        expected.add("|--SendingTime[52] = 20100318-03:21:11.364");
        expected.add("|--MDReqID[262] = A");
        expected.add("|--NoMDEntries[268] = 2");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = BID[0]");
        expected.add("  |--MDEntryID[278] = BID");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37215");
        expected.add("  |--Currency[15] = EUR");
        expected.add("  |--MDEntrySize[271] = 2500000");
        expected.add("  |--NumberOfOrders[346] = 1");
        expected.add("  +--MDUpdateAction[279] = NEW[0]");
        expected.add("  |--MDEntryType[269] = OFFER[1]");
        expected.add("  |--MDEntryID[278] = OFFER");
        expected.add("  |--Symbol[55] = EUR/USD");
        expected.add("  |--MDEntryPx[270] = 1.37224");
        expected.add("  |--Currency[15] = EUR");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldNotRenderNonFixMessage()
    {
        final List<String> strings = fixRenderer.renderBottomPaneContents("This is not a valid log line.");
        assertThat(strings).isEmpty();

    }

    @Test
    void shouldRenderFixMsgWithCustomFixXml() throws IOException
    {

        final String fixXmlStr = "<fix>\n" +
                                 "  <messages>\n" +
                                 "    <message name=\"fakemessagetype1\" msgtype=\"0\" msgcat=\"app\">\n" +
                                 "      <field name=\"MsgType\" required=\"Y\"/>\n" +
                                 "      <field name=\"BeginString\" required=\"Y\"/>\n" +
                                 "      <field name=\"BodyLength\" required=\"Y\"/>\n" +
                                 "      <field name=\"CheckSum\" required=\"Y\"/>\n" +
                                 "      <field name=\"FakeEnum\" required=\"Y\"/>\n" +
                                 "      <field name=\"FakeField1\" required=\"N\"/>\n" +
                                 "      <field name=\"FakeField2\" required=\"N\"/>\n" +
                                 "      <field name=\"FakeField3\" required=\"N\"/>\n" +
                                 "    </message>\n" +
                                 "  </messages>\n" +
                                 "  <fields>\n" +
                                 "    <field number=\"8\" name=\"BeginString\" type=\"STRING\"/>\n" +
                                 "    <field number=\"9\" name=\"BodyLength\" type=\"LENGTH\"/>\n" +
                                 "    <field number=\"10\" name=\"CheckSum\" type=\"STRING\"/>" +
                                 "    <field number=\"20006\" name=\"FakeEnum\" type=\"INT\">\n" +
                                 "      <value enum=\"0\" description=\"Fake1\"/>\n" +
                                 "      <value enum=\"1\" description=\"Fake2\"/>\n" +
                                 "      <value enum=\"2\" description=\"Fake3\"/>\n" +
                                 "    </field>\n" +
                                 "    <field number=\"35\" name=\"MsgType\" type=\"STRING\">\n" +
                                 "      <value enum=\"0\" description=\"fakemessagetype1\"/>\n" +
                                 "      <value enum=\"1\" description=\"fakemessagetype2\"/>\n" +
                                 "      <value enum=\"2\" description=\"fakemessagetype3\"/>\n" +
                                 "    </field>\n" +
                                 "    <field number=\"20007\" name=\"FakeField1\" type=\"CURRENCY\"/>\n" +
                                 "    <field number=\"20008\" name=\"FakeField2\" type=\"NUMINGROUP\"/>\n" +
                                 "    <field number=\"20009\" name=\"FakeField3\" type=\"STRING\"/>\n" +
                                 "  </fields>\n" +
                                 "</fix>";

        final File fixXml = File.createTempFile("FixRendererTest", "" + System.currentTimeMillis());
        Files.write(fixXml.toPath(), Arrays.asList(fixXmlStr.split("\n")));
        final FIXRenderer fixRenderer = FIXRenderer.createFIXRenderer(fixXml.toString());
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.CUSTOM\u000135=0\u000120006=1\u000120008=JUNIOR\u000120007=FRIDGE" +
                        "\u000110=50\u0001");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.CUSTOM");
        expected.add("|--MsgType[35] = fakemessagetype1[0]");
        expected.add("|--FakeEnum[20006] = Fake2[1]");
        expected.add("|--FakeField2[20008] = JUNIOR");
        expected.add("|--FakeField1[20007] = FRIDGE");
        expected.add("|--CheckSum[10] = 50");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRenderFixMsgWithExtraFixXml() throws IOException
    {
        final String fixXmlStr =
                "<fix>\n" +
                "  <messages>\n" +
                "    <message name=\"fakemessagetype1\" msgtype=\"0\" msgcat=\"app\">\n" +
                "      <field name=\"MsgType\" required=\"Y\"/>\n" +
                "      <field name=\"BeginString\" required=\"Y\"/>\n" +
                "      <field name=\"BodyLength\" required=\"Y\"/>\n" +
                "      <field name=\"CheckSum\" required=\"Y\"/>\n" +
                "      <field name=\"FakeEnum\" required=\"Y\"/>\n" +
                "      <field name=\"FakeField1\" required=\"N\"/>\n" +
                "      <field name=\"FakeField2\" required=\"N\"/>\n" +
                "      <field name=\"FakeField3\" required=\"N\"/>\n" +
                "    </message>\n" +
                "  </messages>\n" +
                "  <fields>\n" +
                "    <field number=\"8\" name=\"BeginString\" type=\"STRING\"/>\n" +
                "    <field number=\"9\" name=\"BodyLength\" type=\"LENGTH\"/>\n" +
                "    <field number=\"10\" name=\"CheckSum\" type=\"STRING\"/>\n" +
                "    <field number=\"20006\" name=\"FakeEnum\" type=\"INT\"/>\n" +
                "    <field number=\"35\" name=\"MsgType\" type=\"STRING\">\n" +
                "      <value enum=\"0\" description=\"fakemessagetype1\"/>\n" +
                "      <value enum=\"1\" description=\"fakemessagetype2\"/>\n" +
                "      <value enum=\"2\" description=\"fakemessagetype3\"/>\n" +
                "    </field>\n" +
                "    <field number=\"20007\" name=\"FakeField1\" type=\"CURRENCY\"/>\n" +
                "    <field number=\"20008\" name=\"FakeField2\" type=\"NUMINGROUP\"/>\n" +
                "    <field number=\"20009\" name=\"FakeField3\" type=\"STRING\"/>\n" +
                "  </fields>\n" +
                "</fix>";
        final String fixXmlFieldDefsStr =
                "<fix>\n" +
                "  <messages/>\n" +
                "  <fields>\n" +
                "    <field number=\"20006\" name=\"FakeEnum\" type=\"INT\">\n" +
                "      <value enum=\"0\" description=\"Fake1\"/>\n" +
                "      <value enum=\"1\" description=\"Fake2\"/>\n" +
                "      <value enum=\"2\" description=\"Fake3\"/>\n" +
                "    </field>\n" +
                "  </fields>\n" +
                "</fix>";

        final File fixXml = File.createTempFile("FixRendererTest", "" + System.currentTimeMillis() + "_1");
        final File fixXmlFieldDefs = File.createTempFile("FixRendererTest", "" + System.currentTimeMillis() + "_2");
        Files.write(fixXml.toPath(), Arrays.asList(fixXmlStr.split("\n")));
        Files.write(fixXmlFieldDefs.toPath(), Arrays.asList(fixXmlFieldDefsStr.split("\n")));
        final FIXRenderer.FixStreamConfig config = new FIXRenderer.FixStreamConfig();
        config.addFixFile(fixXml.toString());
        config.addDefsFixFile(fixXmlFieldDefs.toString());

        final FIXRenderer fixRenderer = new FIXRenderer(config);
        final List<String>
                actual =
                fixRenderer.renderBottomPaneContents(
                        "8=FIX.CUSTOM\u000135=0\u000120006=1\u000120008=JUNIOR\u000120007=FRIDGE" +
                        "\u000110=50\u0001");
        final List<String> expected = new ArrayList<>();
        expected.add("+--BeginString[8] = FIX.CUSTOM");
        expected.add("|--MsgType[35] = fakemessagetype1[0]");
        expected.add("|--FakeEnum[20006] = Fake2[1]");
        expected.add("|--FakeField2[20008] = JUNIOR");
        expected.add("|--FakeField1[20007] = FRIDGE");
        expected.add("|--CheckSum[10] = 50");
        assertThat(actual).isEqualTo(expected);
    }
}