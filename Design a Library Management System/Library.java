import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

// LibraryManagementService: Manages book copies, searching, borrowing,
// returning, and fine calculation.
//
// Book: Stores book details such as ISBN, title, and author.
// BookCopy: Represents an individual physical copy of a book.
// Member: Represents a library member.
// Loan: Tracks a borrowed copy, borrow date, and due date.
//
// Simplifications: In-memory storage, one copy per loan,
// fixed loan period, no reservations or payment processing.

class LibraryManagementService {
    private Map<String, Book> books = new HashMap<>();
    private Map<String, BookCopy> copies = new HashMap<>();
    private Map<String, Member> members = new HashMap<>();
    private Map<String, Loan> loans = new HashMap<>();

    private static final int LOAN_DAYS = 14;
    private static final long FINE_PER_DAY = 5;

    public void addBook(Book book) {
        books.put(book.getIsbn(), book);
    }

    public void addBookCopy(BookCopy copy) {
        if (!books.containsKey(copy.getIsbn())) {
            throw new IllegalArgumentException("Book must be added first");
        }
        copies.put(copy.getCopyId(), copy);
    }

    public void registerMember(Member member) {
        members.put(member.getMemberId(), member);
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> result = new ArrayList<>();
        String query = keyword.toLowerCase();

        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase().contains(query)
                    || book.getAuthor().toLowerCase().contains(query)
                    || book.getIsbn().toLowerCase().contains(query)) {
                result.add(book);
            }
        }

        return result;
    }

    public Loan borrowBook(String memberId, String copyId) {
        Member member = members.get(memberId);
        BookCopy copy = copies.get(copyId);

        if (member == null) {
            throw new IllegalArgumentException("Member not found");
        }
        if (copy == null) {
            throw new IllegalArgumentException("Book copy not found");
        }

        synchronized (copy) {
            if (copy.getStatus() != CopyStatus.AVAILABLE) {
                throw new IllegalStateException("Book copy is not available");
            }

            String loanId = UUID.randomUUID().toString();
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(LOAN_DAYS);

            Loan loan = new Loan(loanId, member, copy, borrowDate, dueDate);

            copy.setStatus(CopyStatus.BORROWED);
            loans.put(loanId, loan);

            return loan;
        }
    }

    public long returnBook(String loanId) {
        Loan loan = loans.get(loanId);

        if (loan == null) {
            throw new IllegalArgumentException("Loan not found");
        }

        synchronized (loan) {
            if (loan.getStatus() != LoanStatus.ACTIVE) {
                throw new IllegalStateException("Book has already been returned");
            }

            LocalDate returnDate = LocalDate.now();
            long overdueDays = Math.max(
                0, ChronoUnit.DAYS.between(loan.getDueDate(), returnDate)
            );

            long fine = overdueDays * FINE_PER_DAY;

            loan.setReturnDate(returnDate);
            loan.setStatus(LoanStatus.RETURNED);
            loan.getCopy().setStatus(CopyStatus.AVAILABLE);

            return fine;
        }
    }

    public List<Loan> getMemberLoans(String memberId) {
        if (!members.containsKey(memberId)) {
            throw new IllegalArgumentException("Member not found");
        }

        List<Loan> result = new ArrayList<>();

        for (Loan loan : loans.values()) {
            if (loan.getMember().getMemberId().equals(memberId)) {
                result.add(loan);
            }
        }

        return result;
    }
}

class Book {
    private final String isbn;
    private final String title;
    private final String author;

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (ISBN: " + isbn + ")";
    }
}

class BookCopy {
    private final String copyId;
    private final String isbn;
    private CopyStatus status;

    public BookCopy(String copyId, String isbn) {
        this.copyId = copyId;
        this.isbn = isbn;
        this.status = CopyStatus.AVAILABLE;
    }

    public String getCopyId() {
        return copyId;
    }

    public String getIsbn() {
        return isbn;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }
}

class Member {
    private final String memberId;
    private final String name;

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }
}

class Loan {
    private final String loanId;
    private final Member member;
    private final BookCopy copy;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;

    private LocalDate returnDate;
    private LoanStatus status;

    public Loan(String loanId, Member member, BookCopy copy,
                LocalDate borrowDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.member = member;
        this.copy = copy;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = LoanStatus.ACTIVE;
    }

    public String getLoanId() {
        return loanId;
    }

    public Member getMember() {
        return member;
    }

    public BookCopy getCopy() {
        return copy;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Loan ID: " + loanId
            + ", Member: " + member.getName()
            + ", Copy: " + copy.getCopyId()
            + ", Due: " + dueDate
            + ", Status: " + status;
    }
}

enum CopyStatus {
    AVAILABLE,
    BORROWED
}

enum LoanStatus {
    ACTIVE,
    RETURNED
}

public class Main {
    public static void main(String[] args) {
        LibraryManagementService library = new LibraryManagementService();

        Book book = new Book("ISBN101", "Clean Code", "Robert C. Martin");
        library.addBook(book);

        library.addBookCopy(new BookCopy("COPY1", "ISBN101"));
        library.addBookCopy(new BookCopy("COPY2", "ISBN101"));

        Member member = new Member("M1", "Alice");
        library.registerMember(member);

        System.out.println("Search results:");
        for (Book result : library.searchBooks("clean")) {
            System.out.println(result);
        }

        Loan loan = library.borrowBook("M1", "COPY1");
        System.out.println("\nBorrowed book:");
        System.out.println(loan);

        long fine = library.returnBook(loan.getLoanId());
        System.out.println("\nReturn fine: ₹" + fine);

        System.out.println("\nMember loan history:");
        for (Loan memberLoan : library.getMemberLoans("M1")) {
            System.out.println(memberLoan);
        }
    }
}