/*
 * Patient Lookup directive
 *
 * Define an element patient-lookup with an attribute notify
 * to perform a patient search and call the notify function
 * when the patient is found. The element generates the html
 * in patient-lookup.html.
 *
 * Usage: <patient-lookup notify="callback">
 */

( function() {
  'use strict';

  angular.module('PatientLookup', ['ChoirApi'])
          .controller('PatientLookupCtrl', ['choirApi', PatientLookupCtrl])
          .directive('patientLookup', PatientLookup);

  function PatientLookupCtrl(choirApi) {
    // Saved callback to be called when a patient is found
    var notify = null;

    var vm = this;
    vm.searchText = '';
    vm.patients = [];
    vm.lookup = lookup;
    vm.select = select;

    /*
     * find the patient based on the search text
     */
    function lookup(callback) {
      notify = callback;
      vm.searchText = vm.searchText.trim();
      if (vm.searchText.length > 0) {
        choirApi.findPatient(vm.searchText,
                function(result) {
                  vm.patients = result.patients;
                  if (vm.patients.length == 1) {
                    // One patient found, select that patient
                    notify(vm.patients[0]);
                    vm.searchText = '';
                  } else {
                    // No or multiple patients found, show dialog
                    $('#lookupDialog').modal('show');
                  }
                }
        );
      }
    }

    /*
     * Select the patient from the dialog
     */
    function select(patient) {
      notify(patient);
      vm.searchText = '';
      $('#lookupDialog').modal('hide');
    }

    /*
     * Cancel the dialog
     */
    function cancel() {
      $('#lookupDialog').modal('hide');
    }
  }

  /*
   * patient-lookup Directive definition
   */
  function PatientLookup() {
    return {
      restrict: 'E',
      scope: {
        notify: '=notify'
      },
      templateUrl : 'patient-lookup.html'
    };
  }

})();