package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.DB.SQLiteDB;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO implements TransactionDAO {
    private SQLiteDB DB;

    private static final String TransactionDetails = "transactions";

    private static final String TransactionDate = "date";
    private static final String TransactionAccountNo = "accountno";
    private static final String TransactionExpenseType = "expenseType";
    private static final String TransactionAmount = "amount";

    public PersistentTransactionDAO(SQLiteDB db){
        this.DB = db;
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        if(expenseType == ExpenseType.EXPENSE){
            PersistentAccountDAO parsAccount = new PersistentAccountDAO(this.DB);
            try {
                Account user = parsAccount.getAccount(accountNo);
                if(user.getBalance() < amount){
                    return;
                }
            }catch (Exception e){
                System.out.println("Invalid Account Number");
            }
        }

        String strDate = date.toString();
        ContentValues transactionContent = new ContentValues();
        transactionContent.put(TransactionAccountNo, accountNo);
        transactionContent.put(TransactionDate, strDate);
        transactionContent.put(TransactionExpenseType, getStringExpense(expenseType));
        transactionContent.put(TransactionAmount, amount);
        this.DB.insertData(TransactionDetails, transactionContent);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        Cursor result = this.DB.getData(TransactionDetails,new String[] {"*"},new String[][] {});
        List<Transaction> transactions = new ArrayList<Transaction>();

        if(result.getCount() != 0) {
            while (result.moveToNext()) {
                transactions.add(new Transaction(stringToDate(
                        result.getString(result.getColumnIndex(TransactionDate))),
                        result.getString(result.getColumnIndex(TransactionAccountNo)),
                        getExpense(result.getString(result.getColumnIndex(TransactionExpenseType))),
                        result.getDouble(result.getColumnIndex(TransactionAmount)) ));
            }
        }

        result.close();
        return transactions;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        Cursor result = this.DB.getDataWithLimit(TransactionDetails,new String[] {"*"},new String[][] {},limit);
        List<Transaction> transactions = new ArrayList<Transaction>();
        if(result.getCount() != 0) {

            while (result.moveToNext()) {
                transactions.add(new Transaction(stringToDate(
                        result.getString(result.getColumnIndex(TransactionDate))),
                        result.getString(result.getColumnIndex(TransactionAccountNo)),
                        getExpense(result.getString(result.getColumnIndex(TransactionExpenseType))),
                        result.getDouble(result.getColumnIndex(TransactionAmount)) ));
            }
        }
        result.close();
        return transactions;
    }

    public ExpenseType getExpense(String expense){
        if(expense.equals("Expense")){
            return ExpenseType.EXPENSE;
        }else{
            return ExpenseType.INCOME;
        }
    }

    public String getStringExpense(ExpenseType expense){
        if(expense == ExpenseType.EXPENSE){
            return "Expense";
        }else{
            return "Income";
        }
    }

    public Date stringToDate(String strDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        Date date = new Date();

        try{
            date = dateFormat.parse(strDate);
        }catch(Exception e){
            System.out.println(e);
        }

        return date;
    }

    public void removeTransaction(String transactionNo) throws InvalidAccountException {
        int result = this.DB.deleteData(TransactionDetails,"transaction_no",transactionNo);
        if(result == 0){
            throw new InvalidAccountException("Invalid Transaction Number");
        }
    }
}
