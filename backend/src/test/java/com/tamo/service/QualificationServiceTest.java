package com.tamo.service;
import com.google.gson.*;
import com.tamo.model.*;
import com.tamo.util.JsonUtil;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class QualificationServiceTest{
 DataStore ds;JsonDataService data;QualificationService service;
 @BeforeEach void setup(){ds=TestDataFactory.createTestDataStore();data=mock(JsonDataService.class);when(data.getData()).thenReturn(ds);service=new QualificationService(data);}
 @Test void snapshotIsIndependentOfLaterEdits(){JsonObject snap=service.snapshot("u001");JsonObject update=service.get("u001");update.addProperty("program","Changed program");service.save("u001",update);assertEquals("Synthetic program",snap.get("program").getAsString());ds.getProfiles().get(0).setGpa(1);assertNotEquals(1,snap.getAsJsonObject("academicProfile").get("gpa").getAsDouble());assertFalse(snap.has("password"));}
 @Test void validatesLevelsIdentityAcknowledgmentAndLists(){JsonObject q=service.get("u001");for(String[] bad:new String[][]{{"studentLevel","other"},{"uciStudentId","123"},{"uciEmail","bad"}}){JsonObject copy=q.deepCopy();copy.addProperty(bad[0],bad[1]);assertThrows(QualificationService.Problem.class,()->service.save("u001",copy));}q.addProperty("acknowledged",false);assertThrows(QualificationService.Problem.class,()->service.save("u001",q));}
 @Test void missingHistoricalSnapshotRoundTripsWithoutInventingData(){Application a=new Application();Application b=JsonUtil.getGson().fromJson(JsonUtil.getGson().toJson(a),Application.class);assertNull(b.getQualificationSnapshot());ds.getProfiles().get(0).setQualifications(null);assertThrows(QualificationService.Problem.class,()->service.snapshot("u001"));}
}
