package com.betatfamily.mundial_typer.service;

import com.betatfamily.mundial_typer.dto.CupQualificationDto;
import com.betatfamily.mundial_typer.dto.UserRankingDto;
import com.betatfamily.mundial_typer.entity.*;
import com.betatfamily.mundial_typer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        for(CupQualificationDto dto : list.stream().limit(16).toList()) {

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

        for(int i = 0; i < ranking.size(); i++) {
            if(ranking.get(i).getId().equals(user.getId())) {
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
                                     CupMatch source1,
                                     CupMatch source2,
                                     CupStage stage) {

        CupMatch match = new CupMatch();
        match.setStage(stage);
        match.setBracketPosition(position);
        match.setSourceMatch1(source1);
        match.setSourceMatch2(source2);

        return cupMatchRepository.save(match);

    }

    public void generateBracket() {

        cupMatchRepository.deleteAll();

        List<CupSeed> seeds = cupSeedRepository.findAllByOrderBySeedAsc();

        // 1/8 finału
        CupMatch of1 = createFirstRound(1, seeds.get(0), seeds.get(15));
        CupMatch of2 = createFirstRound(2, seeds.get(7), seeds.get(8));
        CupMatch of3 = createFirstRound(3, seeds.get(3), seeds.get(12));
        CupMatch of4 = createFirstRound(4, seeds.get(4), seeds.get(11));

        CupMatch of5 = createFirstRound(5, seeds.get(1), seeds.get(14));
        CupMatch of6 = createFirstRound(6, seeds.get(6), seeds.get(9));
        CupMatch of7 = createFirstRound(7, seeds.get(2), seeds.get(13));
        CupMatch of8 = createFirstRound(8, seeds.get(5), seeds.get(10));

        // 1/4 finału
        createNextRound(9, of1, of2, CupStage.QUARTER_FINAL);
        createNextRound(10, of3, of4, CupStage.QUARTER_FINAL);
        createNextRound(11, of5, of6, CupStage.QUARTER_FINAL);
        createNextRound(12, of7, of8, CupStage.QUARTER_FINAL);

        // 1/2 finału
        List<CupMatch> qf = cupMatchRepository.findByStageOrderByBracketPosition(CupStage.QUARTER_FINAL);
        createNextRound(13, qf.get(0), qf.get(1), CupStage.SEMI_FINAL);
        createNextRound(14, qf.get(2), qf.get(3), CupStage.SEMI_FINAL);

        // Finał
        List<CupMatch> sf = cupMatchRepository.findByStageOrderByBracketPosition(CupStage.SEMI_FINAL);
        createNextRound(15, sf.get(0), sf.get(1), CupStage.FINAL);

    }

    public List<CupMatch> getBracket() {

        return cupMatchRepository
                .findAll()
                .stream()
                .sorted(Comparator
                        .comparing(CupMatch::getStage)
                        .thenComparing(CupMatch::getBracketPosition))
                .toList();
    }

    public CupMatch getCupMatch(Long id){

        return cupMatchRepository.findById(id).orElseThrow();
    }
}
