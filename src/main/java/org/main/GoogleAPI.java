package org.main;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleAPI {
    public static String APPLICATION_NAME = "Google Sheets Example";
    public static String SPREADSHEET_ID = "1OulK7qcztNx3LtyS1zSeCjprHqocoHYLQiAc4GdjCOM";

    private static Credential authorize() throws IOException, GeneralSecurityException{
        InputStream in = GoogleAPI.class.getResourceAsStream("/credentials.json");
        //add try loop
        assert in != null;
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(), new InputStreamReader(in)
        );

        List<String> scopes = List.of(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException{
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    public static ValueRange getValueRange(String range_string) throws IOException, GeneralSecurityException {
        Sheets sheetsService = getSheetsService();

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range_string)
                .execute();

        List<List<Object>> values = response.getValues();

//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found");
//        } else {
//            System.out.println("New Data:");
//            for(List<Object> row: values) {
//                System.out.println(row);
//            }
//        }

        return response;
    }
}
