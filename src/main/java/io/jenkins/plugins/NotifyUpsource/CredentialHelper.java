package io.jenkins.plugins.NotifyUpsource;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.security.ACL;
import jenkins.model.Jenkins;

import java.util.List;


public class CredentialHelper {

    public static UsernamePasswordCredentialsImpl findCredentials(String credentialsId) {
        List<DomainRequirement> dummylist = null;
        UsernamePasswordCredentialsImpl credential = CredentialsMatchers.firstOrNull(CredentialsProvider
                .lookupCredentials(UsernamePasswordCredentialsImpl.class, Jenkins.get(), ACL.SYSTEM, dummylist),
                CredentialsMatchers.withId(credentialsId));
        return credential;
    }

    public static String getUrlFromCredentials(UsernamePasswordCredentialsImpl credentials){
        return credentials.getUsername();
    }
}
