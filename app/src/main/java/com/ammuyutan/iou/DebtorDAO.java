package com.ammuyutan.iou;

import com.ammuyutan.iou.Models.Debtor;

import java.util.List;

/***
 * Data Access Object Design Pattern
 * -defines the abstraction on DB operations
 */
public interface DebtorDAO {

    void addDebtor(Debtor debtor);
    void updateDebtor(Debtor debtor);
    int updateBalance(String amount, int id);
    List<Debtor> searchDebtors(String param);
    List<Debtor> getDebtors();
    Debtor getDebtorByPhoneNo(String phoneNo);
    Debtor getDebtorById(int id);
    void deleteDebtor(int id);


}
