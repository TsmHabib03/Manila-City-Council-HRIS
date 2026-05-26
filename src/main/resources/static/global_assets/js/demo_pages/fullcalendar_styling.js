/* ------------------------------------------------------------------------------
 *
 *  # Fullcalendar basic options
 *
 *  Demo JS code for extra_fullcalendar_styling.html page
 *
 * ---------------------------------------------------------------------------- */


// Setup module
// ------------------------------

var FullCalendarStyling = function() {


    //
    // Setup module components
    //

    // External events
    var _componentFullCalendarStyling = function() {
        if (typeof FullCalendar == 'undefined') {
            console.warn('Warning - Fullcalendar files are not loaded.');
            return;
        }


        // Add events
        // ------------------------------

        // Event colors
        var eventColors = [
            {
                title: '1st Quarterly Exam',
                start: '2020-05-01',
                color: '#EC407A'
            },
            {
                title: 'Volcano Model',
                start: '2020-05-04',
                color: '#AB47BC'
            },
            {
                title: 'Iguhit si Jose Rizal',
                start: '2020-05-18',
                color: '#AB47BC'
            },
            {
                title: 'Grade 3 Field Trip',
                start: '2020-05-07',
                end: '2020-05-09',
                color: '#9CCC65'
            },
            {
                id: 999,
                title: 'Online Class Grade 3 Faith',
                start: '2020-05-08T16:00:00',
                color: '#9CCC65'
            },
            {
                id: 999,
                title: 'Online Class Grade 3 Faith',
                start: '2020-05-15T16:00:00',
                color: '#9CCC65'
            },            
            {
                title: 'Meeting',
                start: '2020-05-12T10:30:00',
                end: '2020-05-12T12:30:00',
                color: '#9CCC65'
            },
            {
                title: 'Seminar',
                start: '2020-05-12T13:00:00',
                color: '#9CCC65'
            },
            {
                title: 'Meeting Conference',
                start: '2020-05-12T14:30:00',
                color: '#9CCC65'
            },            
            {
                title: 'Magbigay ng halimbawa ng mga Pandiwa',
                start: '2020-05-13',
                color: '#29B6F6'
            },
            {
                title: 'What are branches of Science',
                start: '2020-05-18',
                color: '#29B6F6'
            },
            {
                title: 'Science Long Quiz # 1',                
                start: '2020-05-18',
                color: '#EC407A'
            },
            {
                title: 'Math Long Quiz # 1',                
                start: '2020-05-28',
                color: '#EC407A'
            }
        ];

        // Event background colors
        var eventBackgroundColors = [
            {
                title: 'All Day Event',
                start: '2020-11-01'
            },
            {
                title: 'Long Event',
                start: '2020-11-07',
                end: '2020-11-10',
                color: '#DCEDC8',
                rendering: 'background'
            },
            {
                id: 999,
                title: 'Repeating Event',
                start: '2020-11-06T16:00:00'
            },
            {
                id: 999,
                title: 'Repeating Event',
                start: '2020-11-16T16:00:00'
            },
            {
                title: 'Conference',
                start: '2020-11-11',
                end: '2020-11-13'
            },
            {
                title: 'Meeting',
                start: '2020-11-12T10:30:00',
                end: '2020-11-12T12:30:00'
            },
            {
                title: 'Lunch',
                start: '2020-11-12T12:00:00'
            },
            {
                title: 'Happy Hour',
                start: '2020-11-12T17:30:00'
            },
            {
                title: 'Dinner',
                start: '2020-11-24T20:00:00'
            },
            {
                title: 'Meeting',
                start: '2020-11-03T10:00:00'
            },
            {
                title: 'Birthday Party',
                start: '2020-11-13T07:00:00'
            },
            {
                title: 'Vacation',
                start: '2020-11-27',
                end: '2020-11-30',
                color: '#FFCCBC',
                rendering: 'background'
            }
        ];


        // Initialization
        // ------------------------------

        //
        // Event colors
        //

        // Define element
        var calendarEventColorsElement = document.querySelector('.fullcalendar-event-colors');

        // Initialize
        if(calendarEventColorsElement) {
            var calendarEventColorsInit = new FullCalendar.Calendar(calendarEventColorsElement, {
                plugins: [ 'dayGrid', 'interaction' ],
                header: {
                    left: 'prev,next today',
                    center: 'title',
                    right: 'dayGridMonth,dayGridWeek,dayGridDay'
                },
                defaultDate: '2020-05-15',
                editable: true,
                events: eventColors
            }).render();
        }


        //
        // Event background colors
        //

        // Define element
        var calendarEventBgColorsElement = document.querySelector('.fullcalendar-background-colors');

        // Initialize
        if(calendarEventBgColorsElement) {
            var calendarEventBgColorsInit = new FullCalendar.Calendar(calendarEventBgColorsElement, {
                plugins: [ 'dayGrid', 'interaction' ],
                header: {
                    left: 'prev,next today',
                    center: 'title',
                    right: 'dayGridMonth,dayGridWeek,dayGridDay'
                },
                defaultDate: '2020-11-12',
                editable: true,
                events: eventBackgroundColors
            }).render();
        }
    };


    //
    // Return objects assigned to module
    //

    return {
        init: function() {
            _componentFullCalendarStyling();
        }
    }
}();


// Initialize module
// ------------------------------

document.addEventListener('DOMContentLoaded', function() {
    FullCalendarStyling.init();
});
