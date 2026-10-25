import java.time.LocalDateTime;
import java.util.*;

// AuctionService: Creates auctions, places bids, and closes auctions.
// Auction: Stores item details, starting price, end time, and current highest bid.
// Bid: Records bidder, amount, and timestamp.
// User: Represents a buyer or seller.
//
// Simplifications: In-memory storage, one highest bid per auction,
// no payment processing, notifications, or automatic scheduler.

class AuctionService {
    private Map<String, Auction> auctions = new HashMap<>();

    public Auction createAuction(
            String auctionId, String itemName, User seller,
            long startingPrice, LocalDateTime endTime) {

        if (startingPrice < 0 || !endTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid auction details");
        }

        if (auctions.containsKey(auctionId)) {
            throw new IllegalArgumentException("Auction ID already exists");
        }

        Auction auction = new Auction(
            auctionId, itemName, seller, startingPrice, endTime
        );

        auctions.put(auctionId, auction);
        return auction;
    }

    public Bid placeBid(String auctionId, User bidder, long amount) {
        Auction auction = getAuction(auctionId);
        return auction.placeBid(bidder, amount);
    }

    public void closeAuction(String auctionId) {
        Auction auction = getAuction(auctionId);
        auction.close();
    }

    public Auction getAuctionDetails(String auctionId) {
        return getAuction(auctionId);
    }

    public List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    private Auction getAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction not found");
        }
        return auction;
    }
}

class Auction {
    private final String auctionId;
    private final String itemName;
    private final User seller;
    private final long startingPrice;
    private final LocalDateTime endTime;

    private Bid highestBid;
    private AuctionStatus status;
    private final List<Bid> bidHistory = new ArrayList<>();

    public Auction(String auctionId, String itemName, User seller,
                   long startingPrice, LocalDateTime endTime) {
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.seller = seller;
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.status = AuctionStatus.ACTIVE;
    }

    public synchronized Bid placeBid(User bidder, long amount) {
        if (status != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }

        if (!LocalDateTime.now().isBefore(endTime)) {
            status = AuctionStatus.CLOSED;
            throw new IllegalStateException("Auction has ended");
        }

        if (bidder.getUserId().equals(seller.getUserId())) {
            throw new IllegalArgumentException("Seller cannot bid on own item");
        }

        long minimumBid = highestBid == null
            ? startingPrice
            : highestBid.getAmount() + 1;

        if (amount < minimumBid) {
            throw new IllegalArgumentException(
                "Bid must be at least " + minimumBid
            );
        }

        Bid bid = new Bid(bidder, amount, LocalDateTime.now());
        highestBid = bid;
        bidHistory.add(bid);

        return bid;
    }

    public synchronized void close() {
        if (status == AuctionStatus.CLOSED) {
            return;
        }

        status = AuctionStatus.CLOSED;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItemName() {
        return itemName;
    }

    public User getSeller() {
        return seller;
    }

    public long getStartingPrice() {
        return startingPrice;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Bid getHighestBid() {
        return highestBid;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public List<Bid> getBidHistory() {
        return Collections.unmodifiableList(new ArrayList<>(bidHistory));
    }

    public User getWinner() {
        return status == AuctionStatus.CLOSED && highestBid != null
            ? highestBid.getBidder()
            : null;
    }

    @Override
    public String toString() {
        return "Auction: " + itemName
            + ", Starting price: " + startingPrice
            + ", Highest bid: "
            + (highestBid == null ? "No bids" : highestBid.getAmount())
            + ", Status: " + status;
    }
}

class Bid {
    private final User bidder;
    private final long amount;
    private final LocalDateTime timestamp;

    public Bid(User bidder, long amount, LocalDateTime timestamp) {
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public User getBidder() {
        return bidder;
    }

    public long getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return bidder.getName() + " bid " + amount
            + " at " + timestamp;
    }
}

class User {
    private final String userId;
    private final String name;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}

enum AuctionStatus {
    ACTIVE,
    CLOSED
}

public class Main {
    public static void main(String[] args) {
        AuctionService service = new AuctionService();

        User seller = new User("U1", "Alice");
        User bob = new User("U2", "Bob");
        User charlie = new User("U3", "Charlie");

        Auction auction = service.createAuction(
            "A1",
            "Gaming Laptop",
            seller,
            30000,
            LocalDateTime.now().plusMinutes(30)
        );

        service.placeBid("A1", bob, 32000);
        service.placeBid("A1", charlie, 35000);
        service.placeBid("A1", bob, 37000);

        System.out.println("Auction details:");
        System.out.println(service.getAuctionDetails("A1"));

        System.out.println("\nBid history:");
        for (Bid bid : auction.getBidHistory()) {
            System.out.println(bid);
        }

        service.closeAuction("A1");

        System.out.println("\nAuction closed.");
        System.out.println("Winner: " +
            (auction.getWinner() == null
                ? "No winner"
                : auction.getWinner().getName()));
    }
}