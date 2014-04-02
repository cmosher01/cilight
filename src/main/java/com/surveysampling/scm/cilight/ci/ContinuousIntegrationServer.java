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

    private final Set<String> jobs;
    private final List<ContinuousIntegrationState> levels;



    public static void main(final String... args) {
        ContinuousIntegrationServer srv = new ContinuousIntegrationServer();
        final ContinuousIntegrationState state = srv.getState();
        System.out.println(state);
        System.out.flush();
    }

    public ContinuousIntegrationServer() {
        final Set<String> lJobs = new HashSet<String>(5,1.0F);
        lJobs.add("dynamix_continuous_compile");
        lJobs.add("p02_01_package");
        lJobs.add("p02_03_test");
        lJobs.add("p02_04_integration_test");
        lJobs.add("p02_06_uat");
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

    private static ContinuousIntegrationState getStateForJob(final String jobName) {
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

    private static String getStateStringFromResultXml(final String xmlResult) {
        final Pattern pat = Pattern.compile("<result>(.*)</result>");
        final Matcher match = pat.matcher(xmlResult);
        if (!match.matches()) {
            return "";
        }

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
