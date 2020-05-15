(function() {
  'use strict';

  angular.module('SurveyPushReport', ['ChoirApi'])
          .controller('SurveyPushReportCtrl', ['choirApi', SurveyPushReportCtrl]);

  var patientId = window.parent.getPatientId();

  function SurveyPushReportCtrl(choirApi) {
    var vm = this;

    // Survey registration list
    vm.surveyRegs = null;
    vm.surveyReg = null;

    // Exposed methods
    vm.refresh = refresh;
    vm.showSurveyReg = showSurveyReg;
    vm.stopSending = stopProcFollowups;

    // Initialization
    refresh();

    /*
     * Refresh the registration list
     */
    function refresh() {
      vm.surveyReg = null;


      var params = {"surveyType" : "ProcBotox",
        "patientId" : patientId };

      choirApi.getReport("surveyRegsPush", params, function(result) {
        console.log(JSON.stringify(result));
        vm.surveyRegs = result.reportDataSet;
        if (vm.surveyRegs.length > 0) {
          console.log("found " + vm.surveyRegs.length + "survey rows");
          //showSurveyReg(vm.surveyregs[0]);
        }
      });
    }

    /*
     * Show the survey registration data
     */
    function showSurveyReg(surveyReg) {
      vm.surveyReg = surveyReg;
    }


    /*
     * Set a survey attribute
     */
    function setAttr(surveyId, name, value) {

        var  attr = {surveyId: surveyId.toString(), name: name, value: value};
        choirApi.updateSurveyAttribute(attr, function(result) {
          refresh();
        });

    }

    function stopProcFollowups(surveyId) {
      var dt = new Date();
      var tm = dt.getTime().toString();
      setAttr(surveyId, 'STOP', tm);

    }
  }

})();
