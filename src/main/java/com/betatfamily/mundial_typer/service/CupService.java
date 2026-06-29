package com.betatfamily.mundial_typer.service;

import com.betatfamily.mundial_typer.dto.CupMatchGameDto;
import com.betatfamily.mundial_typer.dto.CupMatchScoreDto;
import com.betatfamily.mundial_typer.dto.CupQualificationDto;
import com.betatfamily.mundial_typer.dto.UserRankingDto;
import com.betatfamily.mundial_typer.entity.*;
import com.betatfamily.mundial_typer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CupService {

    private final PredictionRepository predictionRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final PointsService pointsService;
    private final CupSeedRepository cupSeedRepository;
    private final CupMatchRepository cupMatchRepository;
    private final RankingService rankingService;

    public List<CupQualificationDto> getQualificationRanking() {

        List<User> users = userRepository.findAll();

        List<Prediction> predictions = predictionRepository.findAllByMatch_Round(MatchRound.GROUP_R3);

        Map<User, Integer> points = new HashMap<>();
        Map<User, Integer> correct3 = new HashMap<>();
        Map<User, Integer> correct1 = new HashMap<>();

        for (User u : users) {
            points.put(u, 0);
            correct3.put(u, 0);
            correct1.put(u, 0);
        }

        for (Prediction p : predictions) {

            int pts = pointsService.calculateMatchPoints(p);
            points.merge(p.getUser(), pts, Integer::sum);

            if (pts == 3) {
                correct3.merge(p.getUser(), 1, Integer::sum);
            }

            if (pts == 1) {
                correct1.merge(p.getUser(), 1, Integer::sum);
            }

        }

        return points.entrySet()
                .stream()
                .map(e -> new CupQualificationDto(
                        e.getKey(),
                        e.getValue(),
                        correct3.get(e.getKey()),
                        correct1.get(e.getKey()),
                        getLeaguePosition(e.getKey())))
                .sorted(Comparator
                        .comparingInt(CupQualificationDto::getPoints)
                        .reversed()
                        .thenComparing(Comparator
                                .comparingInt(CupQualificationDto::getCorrect3)
                                .reversed())
                        .thenComparing(Comparator
                                .comparingInt(CupQualificationDto::getCorrect1)
                                .reversed())
                        .thenComparingInt(CupQualificationDto::getLeaguePosition))
                .toList();
    }

    public List<CupQualificationDto> getFrozenQualificationRanking() {

        return cupSeedRepository
                .findAllByOrderBySeedAsc()
                .stream()
                .map(seed -> new CupQualificationDto(
                        seed.getUser(),
                        seed.getQualificationPoints(),
                        seed.getCorrect3(),
                        seed.getCorrect1(),
                        seed.getLeaguePosition()
                ))
                .toList();
    }

    public void generateSeeds() {

        cupMatchRepository.deleteAll();
        cupSeedRepository.deleteAll();

        List<User> users = userRepository.findAll();

        List<CupQualificationDto> list = new ArrayList<>();

        for (User u : users) {
            list.add(new CupQualificationDto(
                    u,
                    pointsService.calculateCupQualificationPoints(u),
                    pointsService.countCorrect3(u),
                    pointsService.countCorrect1(u),
                    null
            ));
        }

        list.forEach(dto -> dto.setLeaguePosition(getLeaguePosition(dto.getUser())));

        list.sort(
                Comparator.comparingInt(CupQualificationDto::getPoints).reversed()
                        .thenComparing(Comparator.comparingInt(CupQualificationDto::getCorrect3).reversed())
                        .thenComparing(Comparator.comparingInt(CupQualificationDto::getCorrect1).reversed())
                        .thenComparingInt(CupQualificationDto::getLeaguePosition)

        );

        int seed = 1;
        for (CupQualificationDto dto : list) {

            CupSeed cs = new CupSeed();
            cs.setUser(dto.getUser());
            cs.setSeed(seed++);
            cs.setQualificationPoints(dto.getPoints());
            cs.setCorrect3(dto.getCorrect3());
            cs.setCorrect1(dto.getCorrect1());
            cs.setLeaguePosition(dto.getLeaguePosition());

            cupSeedRepository.save(cs);
        }
    }

    private int getLeaguePosition(User user) {

        List<User> users = userRepository.findAll();
        List<Prediction> predictions = predictionRepository.findAll();
        List<UserRankingDto> ranking = rankingService.buildRanking(users, predictions);

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getId().equals(user.getId())) {
                return i + 1;
            }
        }

        return 999;
    }

    private CupMatch createFirstRound(int position,
                                      CupSeed a,
                                      CupSeed b) {

        CupMatch match = new CupMatch();
        match.setStage(CupStage.OF_16);
        match.setBracketPosition(position);
        match.setPlayer1(a.getUser());
        match.setPlayer2(b.getUser());
        match.setPlayer1Seed(a);
        match.setPlayer2Seed(b);

        return cupMatchRepository.save(match);
    }

    private CupMatch createNextRound(int position,
                                     User player1,
                                     User player2,
                                     CupMatch source1,
                                     CupMatch source2,
                                     CupStage stage) {

        CupMatch match = new CupMatch();

        match.setStage(stage);
        match.setBracketPosition(position);

        match.setPlayer1(player1);
        match.setPlayer2(player2);

        match.setSourceMatch1(source1);
        match.setSourceMatch2(source2);

        match.setPlayer1Seed(getSeedFromMatch(source1, player1));
        match.setPlayer2Seed(getSeedFromMatch(source2, player2));

        match.setWinner(null);

        return cupMatchRepository.save(match);
    }

    private CupSeed getSeedFromMatch(CupMatch match,
                                     User winner) {

        if(winner.equals(match.getPlayer1())) {
            return match.getPlayer1Seed();
        }

        return match.getPlayer2Seed();
    }

    public void generateRoundOf16() {

        cupMatchRepository.deleteAll();

        List<CupSeed> seeds = cupSeedRepository.findAllByOrderBySeedAsc();

        // 1/8 finału
        createFirstRound(1, seeds.get(0), seeds.get(15));
        createFirstRound(2, seeds.get(7), seeds.get(8));
        createFirstRound(3, seeds.get(4), seeds.get(11));
        createFirstRound(4, seeds.get(3), seeds.get(12));

        createFirstRound(5, seeds.get(5), seeds.get(10));
        createFirstRound(6, seeds.get(2), seeds.get(13));
        createFirstRound(7, seeds.get(6), seeds.get(9));
        createFirstRound(8, seeds.get(1), seeds.get(14));
    }

    public void generateQuarterFinals() {

        if (!cupMatchRepository
                .findByStageOrderByBracketPosition(CupStage.QUARTER_FINAL)
                .isEmpty()) {
            return;
        }

        List<CupMatch> of16 = cupMatchRepository.findByStageOrderByBracketPosition(CupStage.OF_16);

        createNextRound(9,
                getWinnerFromMatch(of16.get(0)),
                getWinnerFromMatch(of16.get(1)),
                of16.get(0),
                of16.get(1),
                CupStage.QUARTER_FINAL);

        createNextRound(10,
                getWinnerFromMatch(of16.get(2)),
                getWinnerFromMatch(of16.get(3)),
                of16.get(2),
                of16.get(3),
                CupStage.QUARTER_FINAL);

        createNextRound(11,
                getWinnerFromMatch(of16.get(4)),
                getWinnerFromMatch(of16.get(5)),
                of16.get(4),
                of16.get(5),
                CupStage.QUARTER_FINAL);

        createNextRound(12,
                getWinnerFromMatch(of16.get(6)),
                getWinnerFromMatch(of16.get(7)),
                of16.get(6),
                of16.get(7),
                CupStage.QUARTER_FINAL);
    }

    public void generateSemiFinals() {

        if (!cupMatchRepository
                .findByStageOrderByBracketPosition(CupStage.SEMI_FINAL)
                .isEmpty()) {
            return;
        }

        List<CupMatch> qf = cupMatchRepository.findByStageOrderByBracketPosition(CupStage.QUARTER_FINAL);

        createNextRound(13,
                getWinnerFromMatch(qf.get(0)),
                getWinnerFromMatch(qf.get(1)),
                qf.get(0),
                qf.get(1),
                CupStage.SEMI_FINAL);

        createNextRound(14,
                getWinnerFromMatch(qf.get(2)),
                getWinnerFromMatch(qf.get(3)),
                qf.get(2),
                qf.get(3),
                CupStage.SEMI_FINAL);
    }

    public void generateFinal() {

        if (!cupMatchRepository
                .findByStageOrderByBracketPosition(CupStage.FINAL)
                .isEmpty()) {
            return;
        }

        List<CupMatch> sf = cupMatchRepository.findByStageOrderByBracketPosition(CupStage.SEMI_FINAL);

        createNextRound(15,
                getWinnerFromMatch(sf.get(0)),
                getWinnerFromMatch(sf.get(1)),
                sf.get(0),
                sf.get(1),
                CupStage.FINAL);
    }

    public void finishFinal() {

        List<CupMatch> finals = cupMatchRepository.findByStageOrderByBracketPosition(CupStage.FINAL);

        if(finals.isEmpty()) return;

        CupMatch finalMatch = finals.getFirst();
        getWinnerFromMatch(finalMatch);
    }

    public List<CupMatchScoreDto> getBracketWithScores() {

        List<CupMatch> matches = cupMatchRepository
                .findAll()
                .stream()
                .sorted(Comparator
                        .comparing(CupMatch::getStage)
                        .thenComparing(CupMatch::getBracketPosition))
                .toList();

        return matches.stream()
                .map(match -> {

                    User player1 = match.getPlayer1();
                    User player2 = match.getPlayer2();

                    // kolejne rundy
                    if(player1 == null && match.getSourceMatch1() != null) {
                        player1 = getWinnerFromMatch(match.getSourceMatch1());
                    }

                    if(player2 == null && match.getSourceMatch2() != null) {
                        player2 = getWinnerFromMatch(match.getSourceMatch2());
                    }

                    int p1 = 0;
                    int p2 = 0;

                    if (player1 != null) {
                        p1 = calculateCupMatchScore(player1, match.getStage());
                    }

                    if (player2 != null) {
                        p2 = calculateCupMatchScore(player2, match.getStage());
                    }

                    // żeby html dostał zawodników
                    match.setPlayer1(player1);
                    match.setPlayer2(player2);

                    return new CupMatchScoreDto(match, p1, p2);
                })
                .toList();
    }

    private User getWinnerFromMatch(CupMatch match) {

        if(match.getWinner() != null) {
            return match.getWinner();
        }

        User player1 = match.getPlayer1();
        User player2 = match.getPlayer2();
        CupStage stage = match.getStage();

        if(player1 == null || player2 == null) {
            return null;
        }

        int p1 = calculateCupMatchScore(player1, stage);
        int p2 = calculateCupMatchScore(player2, stage);

        User winner;

        if(p1 > p2) {

            winner = player1;

        } else if(p2 > p1) {

            winner = player2;

        } else {

            long player1c3 = countCorrect3(player1, stage);
            long player2c3 = countCorrect3(player2, stage);

            if(player1c3 > player2c3) {

                winner = player1;

            } else if(player2c3 > player1c3) {

                winner = player2;

            } else {

                winner = match.getPlayer1Seed().getSeed() < match.getPlayer2Seed().getSeed()
                        ? match.getPlayer1()
                        : match.getPlayer2();

            }
        }

        match.setWinner(winner);
        cupMatchRepository.save(match);

        return winner;
    }

    private long countCorrect3(User user,
                              CupStage stage) {

        List<MatchRound> rounds = getWorldCupRounds(stage);

        return predictionRepository
                .findByUser(user)
                .stream()
                .filter(p -> rounds.contains(p.getMatch().getRound()))
                .filter(p -> pointsService.calculateMatchPoints(p) == 3)
                .count();
    }

    public CupMatch getCupMatch(Long id) {

        return cupMatchRepository.findById(id).orElseThrow();
    }

    public int calculateCupMatchScore(User user,
                                      CupStage stage) {

        MatchRound round;

        switch (stage) {

            case OF_16:
                round = MatchRound.WORLD_CUP_1_16;
                break;

            case QUARTER_FINAL:
                round = MatchRound.WORLD_CUP_1_8;
                break;

            case SEMI_FINAL:
                round = MatchRound.WORLD_CUP_1_4;
                break;

            case FINAL:
                return predictionRepository
                        .findByUser(user)
                        .stream()
                        .filter(p ->
                                p.getMatch().getRound() == MatchRound.WORLD_CUP_1_2 ||
                                p.getMatch().getRound() == MatchRound.WORLD_CUP_BRONZE ||
                                p.getMatch().getRound() == MatchRound.WORLD_CUP_FINAL
                        )
                        .mapToInt(pointsService::calculateMatchPoints)
                        .sum();

            default:
                return 0;
        }

        MatchRound finalRound = round;

        return predictionRepository
                .findByUser(user)
                .stream()
                .filter(p -> p.getMatch().getRound() == finalRound)
                .mapToInt(pointsService::calculateMatchPoints)
                .sum();
    }

    private int calculatePointsForRounds(User user,
                                         List<MatchRound> rounds) {

        return predictionRepository.findByUser(user)
                .stream()
                .filter(p -> rounds.contains(p.getMatch().getRound()))
                .mapToInt(pointsService::calculateMatchPoints)
                .sum();
    }

    public CupMatchScoreDto getCupMatchDetails(Long id) {

        CupMatch match = cupMatchRepository.findById(id).orElseThrow();

        int p1 = 0;
        int p2 = 0;

        if(match.getPlayer1() != null) {
            p1 = calculateCupMatchScore(match.getPlayer1(), match.getStage());
        }

        if(match.getPlayer2() != null) {
            p2 = calculateCupMatchScore(match.getPlayer2(), match.getStage());
        }

        CupMatchScoreDto dto = new CupMatchScoreDto(match, p1, p2);

        dto.setGames(getCupGames(match));

        return dto;
    }

    private List<MatchRound> getWorldCupRounds(CupStage stage) {

        return switch (stage) {
            case OF_16 -> List.of(MatchRound.WORLD_CUP_1_16);
            case QUARTER_FINAL -> List.of(MatchRound.WORLD_CUP_1_8);
            case SEMI_FINAL -> List.of(MatchRound.WORLD_CUP_1_4);
            case FINAL -> List.of(
                    MatchRound.WORLD_CUP_1_2,
                    MatchRound.WORLD_CUP_BRONZE,
                    MatchRound.WORLD_CUP_FINAL);
        };
    }

    public List<CupMatchGameDto> getCupGames(CupMatch cupMatch) {

        List<MatchRound> rounds = getWorldCupRounds(cupMatch.getStage());
        List<Match> games = matchRepository.findByRoundInOrderByMatchTimeAsc(rounds);

        return games.stream()
                .map(m -> {

                    Prediction pred1 = predictionRepository
                            .findByUser(cupMatch.getPlayer1())
                            .stream()
                            .filter(p -> p.getMatch().getId().equals(m.getId()))
                            .findFirst()
                            .orElse(null);

                    Prediction pred2 = predictionRepository
                            .findByUser(cupMatch.getPlayer2())
                            .stream()
                            .filter(p -> p.getMatch().getId().equals(m.getId()))
                            .findFirst()
                            .orElse(null);

                    boolean finished = m.getHomeScore() != null;

                    int p1 = finished && pred1 != null
                            ? pointsService.calculateMatchPoints(pred1)
                            : 0;

                    int p2 = finished && pred2 != null
                            ? pointsService.calculateMatchPoints(pred2)
                            : 0;

                    boolean started = m.getMatchTime() != null && m.getMatchTime().isBefore(LocalDateTime.now());

                    return new CupMatchGameDto(
                            m,
                            p1,
                            p2,
                            started && pred1 != null ? pred1.getPredictedHome() : null,
                            started && pred1 != null ? pred1.getPredictedAway() : null,
                            started && pred2 != null ? pred2.getPredictedHome() : null,
                            started && pred2 != null ? pred2.getPredictedAway() : null,
                            started);
                })
                .toList();
    }

    public CupMatch getUserCupMatch(User user) {

        List<CupMatch> matches = cupMatchRepository.findByPlayer1OrPlayer2(user, user);

        return matches.stream()
                .filter(m -> m.getWinner() == null)
                .max(Comparator.comparing(
                        m -> m.getStage().ordinal()
                ))
                .orElse(matches.stream()
                        .max(Comparator.comparing(
                                m -> m.getStage().ordinal()
                        ))
                        .orElse(null)
                );
    }
}
