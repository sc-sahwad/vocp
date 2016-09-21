package com.scryAnalytics.RegFeaturesExtractionController.DAO;

import java.util.List;

public class SegmentFeaturesDAO {

	private List<GateAnnotation> SegmentInstance;
	private List<GateAnnotation> SegmentClass;
	private List<GateAnnotation> Segment;

	public List<GateAnnotation> getSegmentInstance() {
		return SegmentInstance;
	}

	public void setSegmentInstance(List<GateAnnotation> segmentInstance) {
		SegmentInstance = segmentInstance;
	}

	public List<GateAnnotation> getSegmentClass() {
		return SegmentClass;
	}

	public void setSegmentClass(List<GateAnnotation> segmentClass) {
		SegmentClass = segmentClass;
	}

	public List<GateAnnotation> getSegment() {
		return Segment;
	}

	public void setSegment(List<GateAnnotation> segment) {
		Segment = segment;
	}

}
