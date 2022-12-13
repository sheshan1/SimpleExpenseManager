package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.DB.SQLiteDB;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {
    private final SQLiteDB DB;

    private static final String AccountDetails = "account";

    private static final String AccountNo = "accountno";
    private static final String AccountBankName = "bankname";
    private static final String AccountHolderName = "accountHolderName";
    private static final String AccountBalance = "balance";



    public PersistentAccountDAO(SQLiteDB DB){
        this.DB = DB;
    }


    @Override
    public List<String> getAccountNumbersList() {
        Cursor result = this.DB.getData(AccountDetails,new String[] {"accountno"}, new String[][] {});
        List<String> accountNumbers = new ArrayList<String>();

        if(result.getCount() != 0) {
            while (result.moveToNext()) {
                accountNumbers.add(result.getString(0));
            }
        }

        result.close();
        return accountNumbers;
    }

    @Override
    public List<Account> getAccountsList() {
        Cursor result = this.DB.getData(AccountDetails,new String[] {"*"}, new String[][] {});
        List<Account> accounts = new ArrayList<Account>();

        if(result.getCount() != 0) {
            while (result.moveToNext()) {
                accounts.add(new Account(result.getString(0), result.getString(1), result.getString(2), result.getDouble(3)));
            }
        }
        result.close();
        return accounts;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        String[] condition = {"accountNo", "=",accountNo};
        Cursor result = this.DB.getData(AccountDetails,new String[] {"*"}, new String[][] {condition});

        if(result.getCount() == 0){
            throw new InvalidAccountException("Invalid Account Number");
        }

        String acNO = "", bankName = "", accountHolderName = "";
        double balance = 0;

        while(result.moveToNext()){
            acNO = result.getString(result.getColumnIndex(AccountNo));
            bankName = result.getString(result.getColumnIndex(AccountBankName));
            accountHolderName = result.getString(result.getColumnIndex(AccountHolderName));
            balance = result.getDouble(result.getColumnIndex(AccountBalance));
        }

        result.close();
        Account account = new Account(acNO, bankName, accountHolderName, balance);
        return account;
    }

    @Override
    public void addAccount(Account account) {
        ContentValues accountContent = new ContentValues();
        accountContent.put(AccountNo, account.getAccountNo());
        accountContent.put(AccountBankName, account.getBankName());
        accountContent.put(AccountHolderName, account.getAccountHolderName());
        accountContent.put(AccountBalance, account.getBalance());
        this.DB.insertData(AccountDetails, accountContent);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        int result = this.DB.deleteData(AccountDetails, "accountno", accountNo);
        if(result == 0){
            throw new InvalidAccountException("Invalid Account Number");
        }
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        double balance = 0, total = 0;

        try{
            Account acc = getAccount(accountNo);
            balance = acc.getBalance();
        }catch(Exception e){
            throw new InvalidAccountException("Invalid Account Number");
        }

        if (expenseType == ExpenseType.EXPENSE){
            if(balance < amount){
                throw new InvalidAccountException("Account Balance is not Sufficient for this Transaction");
            }else{
                total = balance - amount;
            }
        }else{
            total = amount + balance;
        }

        ContentValues accountContent = new ContentValues();
        accountContent.put(AccountBalance, total);
        String[] condition = {"accountno","=",accountNo};
        if(!this.DB.updateData(AccountDetails, accountContent, condition)){
            throw new InvalidAccountException("Invalid Account Number");
        }
    }
}
