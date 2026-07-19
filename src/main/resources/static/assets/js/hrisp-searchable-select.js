/**
 * Turns any <select class="hrisp-searchable"> into a searchable Select2 combo box.
 * Add data-tags="true" to also allow free-typed values not present in the list
 * (only for selects whose submitted value is plain text, not an entity id).
 * CR Request ID 015 (2026-07-16): Work Experience Office Name list box -> searchable text input.
 */
(function ($) {
    'use strict';

    function initSearchable($scope) {
        $scope.find('select.hrisp-searchable').each(function () {
            var $sel = $(this);
            if ($sel.hasClass('select2-hidden-accessible')) {
                return; // already initialized
            }
            var $modal = $sel.closest('.modal');
            var placeholderText = $.trim($sel.find('option[value=""]').first().text()) || 'Type to search';
            $sel.select2({
                tags: $sel.data('tags') === true || $sel.data('tags') === 'true',
                allowClear: $sel.find('option[value=""]').length > 0,
                placeholder: placeholderText,
                width: '100%',
                dropdownParent: $modal.length ? $modal : $(document.body)
            });
        });
    }

    /**
     * Set a value on a searchable select from JS (edit/showDetails handlers).
     * Creates the option on the fly when the stored value is no longer in the
     * list (legacy free-typed data), then refreshes the Select2 widget.
     */
    window.hrispSelectVal = function (sel, value) {
        var $sel = $(sel);
        value = value == null ? '' : String(value);
        if (value !== '' && $sel.find('option').filter(function () { return this.value === value; }).length === 0) {
            $sel.append(new Option(value, value));
        }
        $sel.val(value).trigger('change');
    };

    $(function () {
        initSearchable($(document));

        // Native form reset does not notify Select2; refresh widgets after reset
        $(document).on('reset', 'form', function () {
            var $form = $(this);
            setTimeout(function () {
                $form.find('select.hrisp-searchable').trigger('change');
            }, 0);
        });
    });
}(jQuery));
