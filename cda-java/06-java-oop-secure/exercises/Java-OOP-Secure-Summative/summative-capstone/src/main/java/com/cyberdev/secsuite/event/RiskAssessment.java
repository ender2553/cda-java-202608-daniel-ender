package com.cyberdev.secsuite.event;

/**
 * The closed set of risk bands a likelihood x impact score (1-25) can fall into (SEC-5).
 * Produced by RiskRegisterService.assess(likelihood, impact).
 *
 * Banding (documented design call -- see also the INSTRUCTOR NOTE on assess):
 * <pre>
 *   raw score = likelihood (1-5) x impact (1-5)
 *    1 -  6  -> LowRisk        achievable scores: 1, 2, 3, 4, 5, 6
 *    7 - 12  -> MediumRisk     achievable scores: 8, 9, 10, 12
 *   13 - 19  -> HighRisk       achievable scores: 15, 16
 *   20 - 25  -> CriticalRisk   achievable scores: 20, 25
 * </pre>
 * (Only products of two integers in 1..5 can actually occur -- 7, 11, 13, 14, 17-19, 21-24
 * never do -- but the bands are written as contiguous ranges so there is no gap for a score
 * to fall through.) Critical therefore requires one axis at 5 and the other at 4 or 5, which
 * matches the usual reading of a 5x5 heat map's top-right corner.
 *
 * INSTRUCTOR NOTE [SEC-5]: THE sealed type of this capstone, deliberately kept OUTSIDE the
 * SecSuiteException hierarchy -- see SecSuiteException's javadoc. A CRITICAL rating is an
 * expected, normal classification (data returned from assess()), not a failure to throw
 * about. Because the permits list is closed, every switch over a RiskAssessment in this code
 * base (RiskRegisterService.remediationWindowDays, ReportService's band label) is exhaustive
 * with NO default branch: add a fifth band and those switches stop compiling until handled.
 * Common mistakes: modelling the bands as exceptions ("throw new CriticalRiskException"), or
 * adding a `default ->` branch that silently swallows a band the author forgot.
 */
public sealed interface RiskAssessment permits LowRisk, MediumRisk, HighRisk, CriticalRisk {

    /** The raw likelihood x impact score (1-25) that produced this band. */
    int score();
}
