function setDohLink(year) {
	
	// Set the href attribute with the extracted year
	var homeLink = document.getElementById('homeLink');
	homeLink.href = contextRoot + '/doh/' + year;
	
	var classificationLink = document.getElementById('classificationLink');
	classificationLink.href = contextRoot + '/doh/genInfoClassification/' + year;

	var qualityManagementLink = document.getElementById('qualityManagementLink');
	qualityManagementLink.href = contextRoot + '/doh/genInfoQualityManagement/' + year;

	var bedCapacityLink = document.getElementById('bedCapacityLink');
	bedCapacityLink.href = contextRoot + '/doh/genInfoBedCapacity/' + year;
	
	var hospOptSummaryOfPatientsLink = document.getElementById('hospOptSummaryOfPatientsLink');
	hospOptSummaryOfPatientsLink.href = contextRoot + '/doh/hospOptSummaryOfPatients/' + year;

	var hospOptDischargesSpecialtyLink = document.getElementById('hospOptDischargesSpecialtyLink');
	hospOptDischargesSpecialtyLink.href = contextRoot + '/doh/hospOptDischargesSpecialty/' + year;

	var hospOptDischargesMorbidityLink = document.getElementById('hospOptDischargesMorbidityLink');
	hospOptDischargesMorbidityLink.href = contextRoot + '/doh/hospOptDischargesMorbidity/' + year;

	var hospOptDischargesNumberDeliveriesLink = document.getElementById('hospOptDischargesNumberDeliveriesLink');
	hospOptDischargesNumberDeliveriesLink.href = contextRoot + '/doh/hospOptDischargesNumberDeliveries/' + year;

	var hospOptDischargesOPVLink = document.getElementById('hospOptDischargesOPVLink');
	hospOptDischargesOPVLink.href = contextRoot + '/doh/hospOptDischargesOPV/' + year;

	var hospOptDischargesOPDLink = document.getElementById('hospOptDischargesOPDLink');
	hospOptDischargesOPDLink.href = contextRoot + '/doh/hospOptDischargesOPD/' + year;

	var hospOptDischargesERLink = document.getElementById('hospOptDischargesERLink');
	hospOptDischargesERLink.href = contextRoot + '/doh/hospOptDischargesER/' + year;

	var hospOptDischargesTestingLink = document.getElementById('hospOptDischargesTestingLink');
	hospOptDischargesTestingLink.href = contextRoot + '/doh/hospOptDischargesTesting/' + year;

	var hospOptDischargesEVLink = document.getElementById('hospOptDischargesEVLink');
	hospOptDischargesEVLink.href = contextRoot + '/doh/hospOptDischargesEV/' + year;

	var hospitalOperationsDeathsLink = document.getElementById('hospitalOperationsDeathsLink');
	hospitalOperationsDeathsLink.href = contextRoot + '/doh/hospitalOperationsDeaths/' + year;

	var hospitalOperationsMortalityDeathsLink = document.getElementById('hospitalOperationsMortalityDeathsLink');
	hospitalOperationsMortalityDeathsLink.href = contextRoot + '/doh/hospitalOperationsMortalityDeaths/' + year;

	var hospitalOperationsHAILink = document.getElementById('hospitalOperationsHAILink');
	hospitalOperationsHAILink.href = contextRoot + '/doh/hospitalOperationsHAI/' + year;

	var hospitalOperationsMajorOptLink = document.getElementById('hospitalOperationsMajorOptLink');
	hospitalOperationsMajorOptLink.href = contextRoot + '/doh/hospitalOperationsMajorOpt/' + year;

	var hospitalOperationsMinorOptLink = document.getElementById('hospitalOperationsMinorOptLink');
	hospitalOperationsMinorOptLink.href = contextRoot + '/doh/hospitalOperationsMinorOpt/' + year;

	var staffingPatternLink = document.getElementById('staffingPatternLink');
	staffingPatternLink.href = contextRoot + '/doh/staffingPattern/' + year;

	var expensesLink = document.getElementById('expensesLink');
	expensesLink.href = contextRoot + '/doh/expenses/' + year;

	var revenuesLink = document.getElementById('revenuesLink');
	revenuesLink.href = contextRoot + '/doh/revenues/' + year;
}