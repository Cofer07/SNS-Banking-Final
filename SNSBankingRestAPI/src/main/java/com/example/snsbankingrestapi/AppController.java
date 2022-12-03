package com.example.snsbankingrestapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;


    @GetMapping("")
    public String viewHomePage(){
        return "login";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new User());

        return "signup_form";
    }

    @PostMapping("/process_register")
    public String processRegister(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepo.save(user);

        addAccount(user, "Savings", 100.00);

        return "register_success";
    }

    @GetMapping("/dashboard")
    public String prepareDashboard(Model model, Authentication authentication) {
        CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();

        // Grabs Details of authenticated user - currently irrelevant
        model.addAttribute("userFullName", customerUserDetails.getFullName());
        model.addAttribute("userId", customerUserDetails.getUserId());

        // get accounts
        //List<Account> accounts1 = customerUserDetails.getUser().getAccounts();
        List<Account> accounts = accountRepository.findByUserId(customerUserDetails.getUserId());
        model.addAttribute("listAccounts", accounts);

//        double getInput=0;
//
//        List<Account> accounts1 = accountRepository.findByUserId(customerUserDetails.getUserId());
//        accounts1.addFunds(customerUserDetails.getUserId(), getInput);

        //get Transactions

        model.addAttribute("Transaction", new Transaction());
        model.addAttribute("Account", new Account());

//        List<Transaction> transactions = transactionRepository.findTransactionBySendingAccountId(customerUserDetails.getUserId());
        List<Transaction> transactions = new ArrayList<>();
        for(Account a: accounts){
            transactions.addAll(getRecentTransactions(a.getAccountid(), 5).get("Combined"));
        }
        transactions.sort(Comparator.comparing(Transaction::getDate));

        if(!transactions.isEmpty()) {
            transactions = transactions.subList(0, Math.min(5, transactions.size()));
        }
//        else{
//            for(int i = 0; i<5; i++){
//                transactions.add(new Transaction());
//            }
//        }
        model.addAttribute("listTransactions", transactions);

//        List<Transaction> transactions = transactionRepository.findTransactionByTransactionId();

        return "dashboard";
    }

    @GetMapping("/history")
    public String prepareTransactionHistory(Model model, Authentication authentication) {
        CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();

        List<Account> accounts = accountRepository.findByUserId(customerUserDetails.getUserId());

        List<Account> checking = new ArrayList<>();
        List<Account> savings = new ArrayList<>();
        List<Account> credit = new ArrayList<>();

        for(Account a: accounts){
            if(a.getType().equals("Checking")){
                checking.add(a);
            }
            if(a.getType().equals("Savings")){
                savings.add(a);
            }
            if((a.getType().equals("Credit"))){
                credit.add(a);
            }
        }

        List<Transaction> checkingTransactions = new ArrayList<>();
        List<Transaction> savingsTransactions = new ArrayList<>();
        List<Transaction> creditTransactions = new ArrayList<>();

        for(Account a: checking){
            checkingTransactions.addAll(getRecentTransactions(a.getAccountid(), 5).get("Combined"));
        }
        for(Account a: savings){
            savingsTransactions.addAll(getRecentTransactions(a.getAccountid(), 5).get("Combined"));
        }
        for(Account a: credit){
            creditTransactions.addAll(getRecentTransactions(a.getAccountid(), 5).get("Combined"));
        }

        checkingTransactions.sort(Comparator.comparing(Transaction::getDate));
        savingsTransactions.sort(Comparator.comparing(Transaction::getDate));
        creditTransactions.sort(Comparator.comparing(Transaction::getDate));

        model.addAttribute("checkingTransactions", checkingTransactions);
        model.addAttribute("savingsTransactions", savingsTransactions);
        model.addAttribute("creditTransactions", creditTransactions);

        return "transaction_history";
    }

//    @PostMapping("/process_login")
//    public String processLogin(Model model){
//        return "dashboard";
//    }

    @GetMapping("money_transfer")
    public String showMoneyTransfer(Model model, Authentication authentication){
        CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();
        List<Account> accounts = accountRepository.findByUserId(customerUserDetails.getUserId());
        model.addAttribute("listAccounts", accounts);
        model.addAttribute("Transaction", new Transaction());
        return "money_transfer";
    }

    @PostMapping("/process_transfer")
    public String processTransfer(@ModelAttribute Transaction transaction, Model model, Authentication authentication){
        addTransaction(transaction.getSendingID(), transaction.getRecievingID(), transaction.getAmount());
        return showMoneyTransfer(model, authentication);
    }

    @GetMapping("transaction_history")
    public String showTransactionHistory(){
        return "transaction_history";
    }

    @GetMapping("profile")
    public String showProfile(Model model, Authentication authentication){
        CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();
        model.addAttribute("user",customerUserDetails.getUser());
        return "profile";
    }

    @PostMapping(path="/add") // Map ONLY POST Requests
    public @ResponseBody String addNewUser (@RequestParam String name
            , @RequestParam String email) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setName(name);
        n.setEmail(email);
        userRepository.save(n);
        return "Saved";
    }

 // Map ONLY POST Requests
    public String addTransaction (Integer from
            , Integer to, Double amount) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        Optional<Account> receivingOptional = accountRepository.findById(to);
        Optional<Account> sendingOptional = accountRepository.findById(from);
        Account receivingAccount = receivingOptional.stream().findFirst().orElse(null);
        Account sendingAccount = sendingOptional.stream().findFirst().orElse(null);

        if(receivingAccount != null && sendingAccount != null){
            Transaction t = new Transaction(sendingAccount, receivingAccount, amount);
            t.setDate(new Date());
            receivingAccount.addReceivedTransaction(t);
            sendingAccount.addSentTransaction(t);

            receivingAccount.setBalance(receivingAccount.getBalance() + amount);
            sendingAccount.setBalance(sendingAccount.getBalance() - amount);

            accountRepository.save(receivingAccount);
            accountRepository.save(sendingAccount);
            transactionRepository.save(t);
            return "Saved";
        }
        else{
            if(receivingAccount == null && sendingAccount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origin account and receiving account do not exist.");
            }
            if(receivingAccount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient account does not exist.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origin account does not exist.");
        }
    }

    @PostMapping("/add_funds")
    public String processAddFunds(@ModelAttribute Transaction transaction, Model model, Authentication authentication){
        Account account = accountRepository.findById(transaction.getRecievingID()).stream().findFirst().orElse(null);
        if(account != null) {
            addFunds(account, transaction.getAmount());
        }
        return prepareDashboard(model, authentication);
    }

    @PostMapping("/add_account")
    public String processAddAccount(@ModelAttribute Account account, Model model, Authentication authentication){
        CustomerUserDetails customerUserDetails = (CustomerUserDetails) authentication.getPrincipal();
        addAccount(customerUserDetails.getUser(),account.getType(), account.getBalance());
        return prepareDashboard(model, authentication);
    }

    public void addFunds ( Account receivingAccount, Double amount) {
        if(receivingAccount != null){
            receivingAccount.setBalance(receivingAccount.getBalance() + amount);

            accountRepository.save(receivingAccount);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient account does not exist.");
    }

    public void addAccount (User user, String type, double amount){
        Account a = new Account(user);

        a.setType(type);
        a.setBalance(amount);
        user.addAccount(a);

        accountRepository.save(a);
    }

    @GetMapping(path="/get-user")
    public @ResponseBody Map<String, String> getUser(@RequestParam int id){
        Optional<User> parent = userRepository.findById(id);
        User user = parent.stream().findFirst().orElse(null);
        if (user != null) {
            Map<String, String> map = new HashMap<>();
            map.put("ID", String.valueOf(user.getUserid()));
            map.put("FirstName", user.getFname());
            map.put("LastName", user.getLname());
            map.put("Email", user.getEmail());
            map.put("PhoneNumber", user.getPhoneNum());
            return map;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with that ID does not exist.");
    }

    @GetMapping(path="/all-transactions")
    public @ResponseBody Map<String, List<Transaction>> getAllTransactions(@RequestParam int accountid) {
        // This returns a JSON or XML with the transactions
        Optional<Account> optionalAccount = accountRepository.findById(accountid);
        Account account = optionalAccount.stream().findFirst().orElse(null);
        if (account != null){
            Map<String, List<Transaction>> map = new HashMap<>();
            map.put("Received", account.getReceived_transactions());
            map.put("Sent", account.getSent_transactions());
            return map;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account with that ID does not exist.");
    }

    @GetMapping(path="/recent-transactions")
    public @ResponseBody Map<String, List<Transaction>> getRecentTransactions(@RequestParam int accountid, @RequestParam int limit) {
        // This returns a JSON or XML with the transactions
        Optional<Account> optionalAccount = accountRepository.findById(accountid);
        Account account = optionalAccount.stream().findFirst().orElse(null);
        if (account != null){
            Map<String, List<Transaction>> map = new HashMap<>();
            List<Transaction> sent = account.getSent_transactions();
            List<Transaction> received = account.getReceived_transactions();
            List<Transaction> combined = account.getSent_transactions();
            combined.addAll(received);

            sent.sort(Comparator.comparing(Transaction::getDate));
            received.sort(Comparator.comparing(Transaction::getDate));
            combined.sort(Comparator.comparing(Transaction::getDate));

            map.put("Received", received.subList(0, Math.min(limit, received.size())));
            map.put("Sent", sent.subList(0, Math.min(limit, sent.size())));
            map.put("Combined", combined.subList(0, Math.min(limit, combined.size())));
            return map;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account with that ID does not exist.");
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

}