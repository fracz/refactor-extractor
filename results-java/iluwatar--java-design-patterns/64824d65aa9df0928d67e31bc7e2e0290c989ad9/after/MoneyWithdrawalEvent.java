package com.iluwatar.event.sourcing.event;

import com.iluwatar.event.sourcing.api.DomainEvent;
import com.iluwatar.event.sourcing.domain.Account;
import com.iluwatar.event.sourcing.state.AccountAggregate;

import java.math.BigDecimal;

/**
 * Created by serdarh on 06.08.2017.
 */
public class MoneyWithdrawalEvent extends DomainEvent {
    private BigDecimal money;
    private int accountNo;

    public MoneyWithdrawalEvent(long sequenceId, long createdTime, int accountNo,BigDecimal money) {
        super(sequenceId, createdTime, "MoneyWithdrawalEvent");
        this.money = money;
        this.accountNo = accountNo;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public int getAccountNo() {
        return accountNo;
    }

    @Override
    public void process() {
        Account account = AccountAggregate.getAccount(accountNo);
        if(account==null){
            throw new RuntimeException("Account not found");
        }
        account.handleEvent(this);
    }
}