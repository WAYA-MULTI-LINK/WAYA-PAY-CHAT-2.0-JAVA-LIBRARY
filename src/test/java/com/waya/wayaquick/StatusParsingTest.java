package com.waya.wayapay;

import com.waya.wayapay.model.CollectionOutcome;
import com.waya.wayapay.model.CollectionStatus;
import com.waya.wayapay.model.PayoutOutcome;
import com.waya.wayapay.model.PayoutStatus;
import com.waya.wayapay.model.WebhookStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusParsingTest {

    @Test
    void collectionStatusParsesCaseInsensitively() {
        assertEquals(CollectionStatus.SUCCESSFUL, CollectionStatus.from("successful"));
        assertEquals(CollectionStatus.PARTIAL, CollectionStatus.from(" PARTIAL "));
        assertEquals(CollectionStatus.UNKNOWN, CollectionStatus.from("something-new"));
        assertEquals(CollectionStatus.UNKNOWN, CollectionStatus.from(null));
    }

    @Test
    void collectionOutcomesAndTerminality() {
        assertEquals(CollectionOutcome.SUCCEEDED, CollectionStatus.SUCCESSFUL.outcome());
        assertTrue(CollectionStatus.SUCCESSFUL.isTerminal());

        assertEquals(CollectionOutcome.IN_FLIGHT, CollectionStatus.PARTIAL.outcome());
        assertFalse(CollectionStatus.PARTIAL.isTerminal());

        assertEquals(CollectionOutcome.NOT_DEBITED, CollectionStatus.DECLINED.outcome());
        assertEquals(CollectionOutcome.INDETERMINATE, CollectionStatus.BANK_ERROR.outcome());
        assertEquals(CollectionOutcome.INDETERMINATE, CollectionStatus.UNKNOWN.outcome());
        assertFalse(CollectionStatus.UNKNOWN.isTerminal());
    }

    @Test
    void payoutStatusOutcomes() {
        assertEquals(PayoutStatus.SUCCESS, PayoutStatus.from("SUCCESS"));
        assertEquals(PayoutOutcome.SUCCEEDED, PayoutStatus.SUCCESS.outcome());
        assertEquals(PayoutOutcome.REVERSED, PayoutStatus.REVERSED.outcome());
        assertEquals(PayoutOutcome.RECONCILING, PayoutStatus.PENDING.outcome());
        assertFalse(PayoutStatus.PENDING.isTerminal());
        assertTrue(PayoutStatus.SUCCESS.isTerminal());
    }

    @Test
    void webhookStatusParses() {
        assertEquals(WebhookStatus.SUCCESSFUL, WebhookStatus.from("SUCCESSFUL"));
        assertEquals(WebhookStatus.FAILED, WebhookStatus.from("failed"));
        assertEquals(WebhookStatus.UNKNOWN, WebhookStatus.from("weird"));
    }
}
