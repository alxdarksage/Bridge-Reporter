package org.sagebionetworks.bridge.reporter.worker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import org.sagebionetworks.bridge.reporter.helper.BridgeHelper;
import org.sagebionetworks.bridge.reporter.request.ReportType;
import org.sagebionetworks.bridge.rest.model.Study;
import org.sagebionetworks.bridge.rest.model.Upload;
import org.sagebionetworks.bridge.rest.model.UploadStatus;

import com.google.common.collect.Lists;

public class UploadsReportGeneratorTest {
    
    private static final String STUDY_ID = "studyId";
    private static final DateTime START_DATE = DateTime.parse("2017-06-09T00:00:00.000-07:00");
    private static final DateTime END_DATE = DateTime.parse("2017-06-09T23:59:59.999-07:00");

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        BridgeReporterRequest request = new BridgeReporterRequest.Builder()
                .withScheduleType(ReportType.DAILY_SIGNUPS)
                .withScheduler("test-scheduler")
                .withStartDateTime(START_DATE)
                .withEndDateTime(END_DATE).build();
        BridgeHelper bridgeHelper = mock(BridgeHelper.class);

        Study study = mock(Study.class);
        when(study.getIdentifier()).thenReturn(STUDY_ID);
        when(bridgeHelper.getAllStudiesSummary()).thenReturn(Lists.newArrayList(study));
        
        List<Upload> uploads = new ArrayList<>();
        uploads.add(new Upload().recordId("record1").status(UploadStatus.SUCCEEDED));
        uploads.add(new Upload().recordId("record2").status(UploadStatus.REQUESTED));
        when(bridgeHelper.getUploadsForStudy(STUDY_ID, START_DATE, END_DATE)).thenReturn(uploads);
        
        UploadsReportGenerator generator = new UploadsReportGenerator();
        generator.setBridgeHelper(bridgeHelper);
        Report report = generator.generate(request, study);
        
        assertEquals(STUDY_ID, report.getStudyId());
        assertEquals("test-scheduler-daily-signups-report", report.getReportId());
        assertEquals("2017-06-09", report.getDate().toString());
        Map<String, Integer> map = (Map<String, Integer>)report.getData();
        assertEquals(new Integer(1), map.get("requested"));
        assertEquals(new Integer(1), map.get("succeeded"));
        
        verify(bridgeHelper).getUploadsForStudy(STUDY_ID, START_DATE, END_DATE);
    }
}