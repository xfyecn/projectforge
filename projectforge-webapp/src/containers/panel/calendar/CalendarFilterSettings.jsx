import { faCog } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome/index';
import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { Button, Col, Container, Popover, PopoverBody, PopoverHeader, Row } from 'reactstrap';
import UserSelect from '../../../components/base/page/layout/UserSelect';
import CheckBox from '../../../components/design/input/CheckBox';
import style from '../../../components/design/input/Input.module.scss';
import ReactSelect from '../../../components/design/ReactSelect';
import { fetchJsonGet } from '../../../utilities/rest';
import { CalendarContext } from '../../page/calendar/CalendarContext';

/**
 * Settings of a calendar view: time sheet user, default calendar for new events, show holidays etc.
 */
class CalendarFilterSettings extends Component {
    static extractDefaultCalendarValue(props) {
        const { listOfDefaultCalendars, defaultCalendarId } = props;
        return listOfDefaultCalendars.find(it => it.id === defaultCalendarId);
    }

    constructor(props) {
        super(props);
        this.state = {
            popoverOpen: false,
        };

        this.handleDefaultCalendarChange = this.handleDefaultCalendarChange.bind(this);
        this.handleTimesheetUserChange = this.handleTimesheetUserChange.bind(this);
        this.togglePopover = this.togglePopover.bind(this);
        this.handleCheckBoxChange = this.handleCheckBoxChange.bind(this);
    }

    handleCheckBoxChange(event) {
        const { onTimesheetUserChange } = this.props;
        const { saveUpdateResponseInState } = this.context;
        const user = { id: event.target.checked ? 1 : -1 };
        fetchJsonGet('calendar/changeTimesheetUser',
            { userId: user.id },
            (json) => {
                onTimesheetUserChange(user);
                saveUpdateResponseInState(json);
            });
    }

    handleTimesheetUserChange(user) {
        const { onTimesheetUserChange } = this.props;
        const { saveUpdateResponseInState } = this.context;
        const userId = user ? user.id : undefined;
        fetchJsonGet('calendar/changeTimesheetUser',
            { userId },
            (json) => {
                onTimesheetUserChange(user);
                saveUpdateResponseInState(json);
            });
    }

    handleDefaultCalendarChange(value) {
        const { onDefaultCalendarChange } = this.props;
        const { saveUpdateResponseInState } = this.context;
        const id = value ? value.id : '';
        fetchJsonGet('calendar/changeDefaultCalendar',
            { id },
            (json) => {
                onDefaultCalendarChange(id);
                saveUpdateResponseInState(json);
            });
    }

    togglePopover() {
        this.setState(prevState => ({
            popoverOpen: !prevState.popoverOpen,
        }));
    }

    render() {
        const { popoverOpen } = this.state;
        const {
            listOfDefaultCalendars,
            otherTimesheetUsersEnabled,
            timesheetUser,
            translations,
        } = this.props;
        const defaultCalendar = CalendarFilterSettings.extractDefaultCalendarValue(this.props);
        return (
            <React.Fragment>
                <Button
                    id="calendarSettingsPopover"
                    color="link"
                    className="selectPanelIconLinks"
                    onClick={this.togglePopover}
                >
                    <FontAwesomeIcon
                        icon={faCog}
                        className={style.icon}
                        size="lg"
                    />
                </Button>
                <Popover
                    placement="bottom-end"
                    isOpen={popoverOpen}
                    target="calendarSettingsPopover"
                    toggle={this.togglePopover}
                    trigger="legacy"
                >
                    <PopoverHeader>
                        {translations.settings}
                    </PopoverHeader>
                    <PopoverBody>
                        <Container>
                            <Row>
                                <Col>
                                    <ReactSelect
                                        values={listOfDefaultCalendars}
                                        value={defaultCalendar}
                                        label={translations['calendar.defaultCalendar']}
                                        tooltip={translations['calendar.defaultCalendar.tooltip']}
                                        translations={translations}
                                        valueProperty="id"
                                        labelProperty="title"
                                        onChange={this.handleDefaultCalendarChange}
                                    />
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                    {otherTimesheetUsersEnabled ? (
                                        <UserSelect
                                            onChange={this.handleTimesheetUserChange}
                                            value={timesheetUser}
                                            label={translations['calendar.option.timesheeets']}
                                            translations={translations}
                                        />
                                    ) : (
                                        <CheckBox
                                            label={translations['calendar.option.timesheeets']}
                                            id="showTimesheets"
                                            onChange={this.handleCheckBoxChange}
                                            checked={timesheetUser && timesheetUser.id > 0}
                                        />
                                    )}
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                    Optionen: Pausen, Statistik,
                                    Geburtstage, Planungen
                                </Col>
                            </Row>
                        </Container>
                    </PopoverBody>
                </Popover>
            </React.Fragment>
        );
    }
}

CalendarFilterSettings.contextType = CalendarContext;

CalendarFilterSettings.propTypes = {
    onTimesheetUserChange: PropTypes.func.isRequired,
    onDefaultCalendarChange: PropTypes.func.isRequired,
    /* eslint-disable-next-line react/no-unused-prop-types */
    defaultCalendarId: PropTypes.number,
    timesheetUser: PropTypes.shape(),
    otherTimesheetUsersEnabled: PropTypes.bool.isRequired,
    listOfDefaultCalendars: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
    translations: PropTypes.shape({}).isRequired,
};

CalendarFilterSettings.defaultProps = {
    defaultCalendarId: undefined,
    timesheetUser: undefined,
};

export default (CalendarFilterSettings);
