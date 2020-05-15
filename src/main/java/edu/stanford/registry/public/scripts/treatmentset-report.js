(function() {
  'use strict';

  angular.module('TreatmentSetReport', ['ChoirApi'])
          .controller('TreatmentSetReportCtrl', ['$filter','choirApi', TreatmentSetReportCtrl]);


  function TreatmentSetReportCtrl($filter, choirApi) {
    var vm = this;
    vm.fromDt = null;
    vm.fromDt = null;
    vm.toDt = null;

    if (window.parent.getFromTm != null) {
      vm.fromDt = formatDateDisplay(new Date(Number(window.parent.getFromTm())));
    }
    if (window.parent.getToTm != null) {
      vm.toDt =  formatDateDisplay(new Date(Number(window.parent.getToTm())));
    }
    vm.setname = {};
    vm.setname.title = '';
    vm.setname.value  = '';
    vm.setname.valueList = [];

    vm.details = {};
    vm.details.title = '';
    vm.details.value = '';
    vm.details.valueList = [];

    vm.parameters;
    vm.form = null;
    vm.arrayLines = null;

    // methods
    vm.showParameters = showParameters;
    vm.runReport = runReport;

    // Initialization
    showParameters();

    /*
     * Show the report options
     */
    function showParameters() {

      vm.result = null;
      vm.parameters = null;

      choirApi.getReport("tset7days", null, function (result) {

        vm.result = result;
        vm.parameters = result.reportParameters;

        for (var parameter in vm.parameters ) {

          if (vm.parameters[parameter].name === 'setname') {
            vm.setname.title = vm.parameters[parameter].title;
            for (var val in vm.parameters[parameter].VALUELIST) {
              vm.setname.valueList.push( vm.parameters[parameter].VALUELIST[val] );
            }
            vm.setname.value = 'All';
          }

          if (vm.parameters[parameter].name === 'details') {
            vm.details.title = vm.parameters[parameter].title;
            for (var val in vm.parameters[parameter].VALUELIST) {
              vm.details.valueList.push(vm.parameters[parameter].VALUELIST[val]);
            }
            vm.details.value = 'Yes';
          }
        }
      });

    }

    function runReport() {
      if (vm.form !== undefined && vm.form != null) {
        var jsonData = {};
        jsonData["fromDt"] = formatDateParam(vm.fromDt, 0, 0); // start of day
        jsonData["toDt"] = formatDateParam(vm.toDt, 23, 59); // end of day
        jsonData["setName"] = vm.setname.value;
        jsonData["details"] = vm.details.value;
        choirApi.getReport("tset7days", JSON.stringify(jsonData), function (result) {
        vm.arrayLines = result.reportDataSet;
        });
      }
    }
  }
})();
