$(document).ready(function(){
	initSubjectTable();
});

function initSubjectTable(){
	var temp = $("#parent_tmp tbody").html(); // get from template
	
	var obj1 = new Object;
	obj1.subject = "Math";
	obj1.teacher = "Chris Lenovo";
	obj1.exam = "10/10";
	obj1.homework = "6/10";
	obj1.project = "2/5";
	
	var obj2 = new Object;
	obj2.subject = "Recess";
	obj2.teacher = "None";
	obj2.exam = "10/10";
	obj2.homework = "10/10";
	obj2.project = "5/5";
	
	var obj3 = new Object;
	obj3.subject = "TLE";
	obj3.teacher = "Escapade Maril";
	obj3.exam = "8/10";
	obj3.homework = "10/10";
	obj3.project = "0/5";
	
	var testArray = [];
	testArray.push(obj1);
	testArray.push(obj2);
	testArray.push(obj3);
	
	var $parentSubjList = $("#parent_subject_list");
	$parentSubjList.empty();
	
	testArray.forEach(function(obj){
		var tmp = temp;
		tmp = tmp.replace(/\*SUBJECTNAME\*/g,obj.subject);
		tmp = tmp.replace(/\*TEACHERNAME\*/g,obj.teacher);
		tmp = tmp.replace(/\*EXAMS\*/g,obj.exam);
		tmp = tmp.replace(/\*HOMEWORKS\*/g,obj.homework);
		tmp = tmp.replace(/\*PROJECTS\*/g,obj.project);
		$parentSubjList.append(tmp);
	});
}