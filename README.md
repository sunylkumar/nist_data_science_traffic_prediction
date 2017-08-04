# Traffic Event Prediction


## Description
	Given event information in the past years, you are asked to predict future event occurrence.
## Event Data
	event_id: uniquely identify an event, e.g.
		“MDOT_CHART_4aff02b300110095003f0be8b3035daa”
	event_description: a text description about an event, e.g. “Disabled Vehicle Event @ I-495 ATMD 187”
	Timestamps: times the event was created, confirmed, and closed (some are missing). You should use closed_timestamp in this project.
	event_type: the type of an event, e.g. “accidentsAndIncidents”.
	geographical location: (latitude, longitude)
	There are 9 other less important fields.
