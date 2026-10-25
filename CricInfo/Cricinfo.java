import java.util.*;

/*
 * CricInfo - 25-minute LLD
 *
 * 1. CricInfoService - creates matches and records deliveries.
 * 2. Match - stores teams and innings.
 * 3. Innings - tracks runs, wickets, and overs.
 * 4. Delivery - represents one ball and its outcome.
 * 5. Team / Player - represent teams and players.
 *
 * Flow:
 * Create match -> Start innings -> Record delivery -> View score.
 *
 * Simplification:
 * Each delivery represents a legal ball.
 * Extras and detailed cricket rules are omitted.
 */

public class Main {
    public static void main(String[] args) {
        CricInfoService service = new CricInfoService();

        Team india = new Team("India");
        Team australia = new Team("Australia");

        india.addPlayer(new Player("P1", "Rohit"));
        india.addPlayer(new Player("P2", "Virat"));

        Match match = service.createMatch("M1", india, australia);

        match.startInnings(india);

        service.recordDelivery("M1", 1, 0);
        service.recordDelivery("M1", 4, 0);
        service.recordDelivery("M1", 6, 0);
        service.recordDelivery("M1", 0, 1);

        service.showScore("M1");
    }
}

class CricInfoService {
    private Map<String, Match> matches = new HashMap<>();

    public Match createMatch(String id, Team team1, Team team2) {
        Match match = new Match(id, team1, team2);
        matches.put(id, match);
        return match;
    }

    public void recordDelivery(String matchId, int runs, int wickets) {
        Match match = matches.get(matchId);

        if (match == null) {
            throw new IllegalArgumentException("Match not found");
        }

        match.recordDelivery(runs, wickets);
    }

    public void showScore(String matchId) {
        Match match = matches.get(matchId);

        if (match == null) {
            throw new IllegalArgumentException("Match not found");
        }

        match.showScore();
    }
}

class Match {
    private String id;
    private Team team1;
    private Team team2;
    private Team battingTeam;
    private Innings innings;

    public Match(String id, Team team1, Team team2) {
        this.id = id;
        this.team1 = team1;
        this.team2 = team2;
    }

    public void startInnings(Team battingTeam) {
        if (battingTeam != team1 && battingTeam != team2) {
            throw new IllegalArgumentException("Invalid batting team");
        }

        this.battingTeam = battingTeam;
        this.innings = new Innings(battingTeam);
    }

    public void recordDelivery(int runs, int wickets) {
        if (innings == null) {
            throw new IllegalStateException("Innings not started");
        }

        innings.recordDelivery(runs, wickets);
    }

    public void showScore() {
        if (innings == null) {
            System.out.println("Match has not started");
            return;
        }

        innings.showScore();
    }
}

class Innings {
    private Team battingTeam;
    private int runs;
    private int wickets;
    private int balls;
    private List<Delivery> deliveries = new ArrayList<>();

    public Innings(Team battingTeam) {
        this.battingTeam = battingTeam;
    }

    public void recordDelivery(int runs, int wickets) {
        if (runs < 0 || wickets < 0 || wickets > 1) {
            throw new IllegalArgumentException("Invalid delivery");
        }

        deliveries.add(new Delivery(runs, wickets));
        this.runs += runs;
        this.wickets += wickets;
        this.balls++;
    }

    public void showScore() {
        System.out.println(
            battingTeam.getName() + ": " + runs + "/" + wickets
            + " (" + balls / 6 + "." + balls % 6 + " overs)"
        );
    }
}

class Delivery {
    private int runs;
    private int wickets;

    public Delivery(int runs, int wickets) {
        this.runs = runs;
        this.wickets = wickets;
    }
}

class Team {
    private String name;
    private List<Player> players = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public String getName() {
        return name;
    }
}

class Player {
    private String id;
    private String name;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }
}