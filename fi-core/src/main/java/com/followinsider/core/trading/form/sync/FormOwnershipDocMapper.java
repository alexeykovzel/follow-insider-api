package com.followinsider.core.trading.form.sync;

import com.followinsider.common.utils.StringUtils;
import com.followinsider.core.trading.company.Company;
import com.followinsider.core.trading.form.Form;
import com.followinsider.core.trading.insider.Insider;
import com.followinsider.core.trading.trade.Trade;
import com.followinsider.core.trading.trade.TradeType;
import com.followinsider.secapi.forms.f345.OwnershipDoc;
import com.followinsider.secapi.forms.f345.OwnershipForm;
import com.followinsider.secapi.forms.f345.footnote.Footnote;
import com.followinsider.secapi.forms.f345.footnote.FootnoteContainer;
import com.followinsider.secapi.forms.f345.footnote.FootnoteID;
import com.followinsider.secapi.forms.f345.footnote.FootnoteValue;
import com.followinsider.secapi.forms.f345.owner.Issuer;
import com.followinsider.secapi.forms.f345.owner.Relationship;
import com.followinsider.secapi.forms.f345.owner.ReportingOwner;
import com.followinsider.secapi.forms.f345.transaction.PostTransactionAmounts;
import com.followinsider.secapi.forms.f345.transaction.Transaction;
import com.followinsider.secapi.forms.f345.transaction.nonderivative.NonDerivativeTable;
import com.followinsider.secapi.forms.f345.transaction.nonderivative.NonDerivativeTransaction;
import com.followinsider.secapi.forms.f345.transaction.nonderivative.NonDerivativeTransactionAmounts;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class FormOwnershipDocMapper {

    private static final int MAX_FOOTNOTE_LENGTH = 1000;

    public Form apply(OwnershipDoc doc) {
        OwnershipForm form = doc.ownershipForm();

        return Form.builder()
                .accNum(doc.accNum())
                .filedAt(doc.filedAt())
                .xmlUrl(doc.xmlUrl())
                .company(getCompany(form))
                .insider(getInsider(form))
                .insiderTitles(getInsiderTitles(form))
                .trades(getTrades(form))
                .build();
    }

    private Company getCompany(OwnershipForm form) {
        Issuer issuer = form.getIssuer();

        return Company.builder()
                .cik(issuer.getIssuerCik())
                .name(issuer.getIssuerName())
                .symbol(issuer.getIssuerTradingSymbol())
                .build();
    }

    // -- Example with multiple reporting owners --
    // https://www.sec.gov/Archives/edgar/data/718937/000091957423006577/xslF345X03/ownership.xml

    // Assume that the 1-st reporting owner is the insider
    private Insider getInsider(OwnershipForm form) {
        ReportingOwner reportingOwner = form.getReportingOwner().get(0);
        ReportingOwner.ID id = reportingOwner.getReportingOwnerId();

        return Insider.builder()
                .cik(id.getRptOwnerCik())
                .name(id.getRptOwnerName())
                .build();
    }

    private Set<String> getInsiderTitles(OwnershipForm form) {
        ReportingOwner reportingOwner = form.getReportingOwner().get(0);
        Relationship relationship = reportingOwner.getReportingOwnerRelationship();

        Set<String> titles = new HashSet<>();
        if (relationship.isDirector()) titles.add("Director");
        if (relationship.isTenPercentOwner()) titles.add("10% Owner");
        if (relationship.isOfficer()) titles.add(relationship.getOfficerTitle());
        if (relationship.isOther()) titles.add(relationship.getOtherText());

        return titles;
    }

    private List<Trade> getTrades(OwnershipForm form) {
        NonDerivativeTable table = form.getNonDerivativeTable();
        if (table == null || table.getNonDerivativeTransaction() == null) {
            return List.of();
        }
        Map<String, String> footnotes = getFootnotes(form);
        return table
                .getNonDerivativeTransaction().stream()
                .map(transaction -> getTrade(transaction, footnotes))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Trade getTrade(NonDerivativeTransaction transaction, Map<String, String> footnotes) {
        TradeType tradeType = getTradeType(transaction);
        if (tradeType == null) return null;

        boolean isDirect = transaction
                .getOwnershipNature()
                .getDirectOrIndirectOwnership()
                .getValue()
                .equals("D");

        if (!isDirect) return null;

        NonDerivativeTransactionAmounts amounts = transaction.getTransactionAmounts();
        String securityTitle = transaction.getSecurityTitle().getValue();
        LocalDate executedAt = transaction.getTransactionDate().getValue();
        Double shareNum = amounts.getTransactionShares().getValue();
        Double sharesLeft = getSharesLeft(transaction);

        FootnoteValue<Double> priceField = amounts.getTransactionPricePerShare();
        FootnoteID priceFootnoteID = priceField.getFootnoteId();

        String priceFootnote = null;
        if (priceFootnoteID != null && priceFootnoteID.getId() != null) {
            priceFootnote = footnotes.get(priceFootnoteID.getId());
            priceFootnote = StringUtils.overflow(priceFootnote, MAX_FOOTNOTE_LENGTH);
        }

        return Trade.builder()
                .securityTitle(securityTitle)
                .type(tradeType)
                .shareNum(shareNum)
                .sharesLeft(sharesLeft)
                .sharePrice(priceField.getValue())
                .sharePriceFootnote(priceFootnote)
                .executedAt(executedAt)
                .build();
    }

    private TradeType getTradeType(NonDerivativeTransaction transaction) {
        Transaction.Coding coding = transaction.getTransactionCoding();
        if (coding == null) return null;

        return switch (coding.getTransactionCode()) {
            case PURCHASE -> TradeType.BUY;
            case SALE -> TradeType.SELL;
            default -> null;
        };
    }

    private Map<String, String> getFootnotes(OwnershipForm form) {
        FootnoteContainer wrapper = form.getFootnotes();
        if (wrapper == null || wrapper.getFootnote() == null) {
            return Map.of();
        }
        Map<String, String> footnotes = new HashMap<>();
        for (Footnote footnote : wrapper.getFootnote()) {
            footnotes.put(footnote.getId(), footnote.getValue());
        }
        return footnotes;
    }

    private Double getSharesLeft(NonDerivativeTransaction transaction) {
        PostTransactionAmounts postAmounts = transaction.getPostTransactionAmounts();
        FootnoteValue<Double> sharesOwnedField = postAmounts.getSharesOwnedFollowingTransaction();

        if (sharesOwnedField != null) {
            return sharesOwnedField.getValue();
        }

        Double sharePrice = transaction
                .getTransactionAmounts()
                .getTransactionPricePerShare()
                .getValue();

        if (sharePrice == null || sharePrice <= 0) return null;

        Double valueLeft = postAmounts
                .getValueOwnedFollowingTransaction()
                .getValue();

        if (valueLeft == null || valueLeft <= 0) return null;

        return valueLeft / sharePrice;
    }

}
