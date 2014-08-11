package com.surveysampling.scm.cilight.ci;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cmosher on 3/31/14.
 */
public class ContinuousIntegrationServer {
    private static final String URL_CI = "http://ctjenkinsm01.surveysampling.com:8080";
    private static final Pattern RESULT_PATTERN = Pattern.compile("<result>(.*)</result>");

    private final Set<String> jobs;
    private final List<ContinuousIntegrationState> levels;
    private final boolean testMode;

    public static void main(final String... args) {
        ContinuousIntegrationServer srv = new ContinuousIntegrationServer(false);
        final ContinuousIntegrationState state = srv.getState();
        System.out.println(state);
        System.out.flush();
    }

    public ContinuousIntegrationServer(boolean testMode) {
        this.testMode = testMode;
        final Set<String> lJobs = new HashSet<String>(5,1.0F);
        lJobs.add("p01_02_deploy");
        lJobs.add("p01_03_test");
        lJobs.add("p02_03_integration");
        lJobs.add("p02_04_uat");
        this.jobs = Collections.<String>unmodifiableSet(lJobs);

        final List<ContinuousIntegrationState> lLevels = new ArrayList<ContinuousIntegrationState>(3);
        lLevels.add(ContinuousIntegrationState.FAILURE);
        lLevels.add(ContinuousIntegrationState.UNSTABLE);
        lLevels.add(ContinuousIntegrationState.SUCCESS);
        Collections.sort(lLevels);
        this.levels = Collections.<ContinuousIntegrationState>unmodifiableList(lLevels);
    }

    public ContinuousIntegrationState getState() {
        for (final ContinuousIntegrationState level : this.levels) {
            for (final String job : this.jobs) {
                final ContinuousIntegrationState state = getStateForJob(job);
                if (state.equals(level)) {
                    return state;
                }
            }
        }

        return ContinuousIntegrationState.UNKNOWN;
    }

    private ContinuousIntegrationState getStateForJob(final String jobName) {
        final URL urlJenkins = buildResultUrl(jobName);
        final String xmlResult = fetchUrl(urlJenkins);
        final String result = getStateStringFromResultXml(xmlResult);
        return parseState(result);
    }

    private static URL buildResultUrl(final String jobName) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(URL_CI).append("/job/").append(jobName).append("/lastBuild/api/xml?xpath=mavenModuleSetBuild/result");
        try {
            return new URL(sb.toString());
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String fetchUrl(final URL url) {
        System.out.printf("%s - Fetching %s\n", new Date(), url);
        final StringBuilder sb = new StringBuilder(32);
        final URLConnection connection;
        BufferedReader in = null;
        try {
            connection = url.openConnection();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            for (String ins = in.readLine(); ins != null; ins = in.readLine()) {
                sb.append(ins);
            }
        } catch (final Throwable e) {
            // just stop reading and return what we have so far
            System.err.println("read \""+sb.toString()+"\" then encountered the following error:");
            System.err.flush();
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private String getStateStringFromResultXml(final String xmlResult) {
        if (testMode) {
            System.out.println(xmlResult);
        }
        final Matcher match = RESULT_PATTERN.matcher(xmlResult);
        if (!match.find()) {
            System.out.printf("%s - no match found\n", new Date());
            return "";
        }
        System.out.printf("%s - Result: %s\n", new Date(), match.group(1));

        return match.group(1);
    }

    private static ContinuousIntegrationState parseState(final String stateString) {
        try {
            return ContinuousIntegrationState.valueOf(stateString);
        } catch (final Throwable e) {
            return ContinuousIntegrationState.UNKNOWN;
        }
    }
}
