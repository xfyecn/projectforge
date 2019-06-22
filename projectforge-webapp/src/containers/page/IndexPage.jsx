import React from 'react';
import { Card, CardBody, CardHeader, CardText, CardTitle, Col, Container, Row } from 'reactstrap';
import { getServiceURL } from '../../utilities/rest';

class IndexPage extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            translations: undefined,
        };

        this.fetchInitial = this.fetchInitial.bind(this);
    }

    componentDidMount() {
        this.fetchInitial();
    }

    fetchInitial() {
        fetch(getServiceURL('index'), {
            method: 'GET',
            credentials: 'include',
            headers: {
                Accept: 'application/json',
            },
        })
            .then(response => response.json())
            .then((json) => {
                const {
                    translations,
                } = json;
                this.setState({
                    translations,
                });
            })
            .catch(error => alert(`Internal error: ${error}`));
    }

    render() {
        const { translations } = this.state;

        const todoDone = { color: 'green' };

        if (!translations) {
            return (<div>{' '}</div>);
        }
        return (
            <Container>
                <Row>
                    <Col>
                        <a href="/wa/">
                            <Card>
                                <CardHeader>
                                    <CardTitle>
                                        {translations['goreact.index.classics.header']}
                                    </CardTitle>
                                </CardHeader>
                                <CardBody>
                                    <CardText>
                                        {translations['goreact.index.classics.body1']}
                                    </CardText>
                                    <CardText>
                                        {translations['goreact.index.classics.body2']}
                                    </CardText>
                                </CardBody>
                            </Card>
                        </a>
                    </Col>
                    <Col>
                        <Card>
                            <CardHeader>
                                <CardTitle>
                                    {translations['goreact.index.react.header']}
                                </CardTitle>
                            </CardHeader>
                            <CardBody>
                                <CardText>
                                    {translations['goreact.index.react.body1']}
                                </CardText>
                                <CardText>
                                    {translations['goreact.index.react.body2']}
                                </CardText>
                            </CardBody>
                        </Card>
                    </Col>
                    <Col>
                        <Card>
                            <CardHeader>
                                <CardTitle>
                                    {translations['goreact.index.both.header']}
                                </CardTitle>
                            </CardHeader>
                            <CardBody>
                                <CardText>
                                    {translations['goreact.index.both.body1']}
                                </CardText>
                                <CardText>
                                    {translations['goreact.index.both.body2']}
                                </CardText>
                            </CardBody>
                        </Card>
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <p style={{
                            marginTop: '10ex',
                            marginBottom: '5ex',
                            color: 'red',
                            fontWeight: 'bold',
                            fontSize: '18px',
                        }}>
                            To-do&apos;s (most have to be done before going public)
                        </p>
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <h1>ToDo&apos;s (Fin)</h1>
                        <ol>
                            <li style={todoDone}>
                                Redirect menu entries on edit page (address - print view)
                            </li>
                            <li>Translations in SearchFilter.jsx</li>
                            <li>Enable localized customized pages (login page, setup page etc.)</li>
                            <li>Display global validation errors of forms</li>
                            <li>
                                Display field validation errors in ReactSelect and date input
                                fields
                            </li>
                            <li>
                                Edit / rename entry of favorite filter, e. g. calendar page (star
                                icon)
                            </li>
                            <li>
                                Clone button (e. g. address): reload page with data sent by server
                                as response after fetching clone
                            </li>
                            <li>
                                Display Logo
                                <ol>
                                    <li>
                                        Configureation: projectforge.properties:
                                        projectforge.logoFile=Micromata.png
                                    </li>
                                    <li>Logo url in rsPublic/systemStatus (e. g. logo.png)</li>
                                    <li>
                                        Logo service: rsPublic/logo.png (rsPublic/&lt;logoUrl&gt;)
                                    </li>
                                </ol>
                            </li>
                            <li>
                                main.chunk.js with hash sum / version id, use service worker for
                                caching app
                            </li>
                            <li>Redirect after logout</li>
                        </ol>
                    </Col>
                    <Col>
                        <h1>ToDo&apos;s (Kai)</h1>
                        <ol>
                            <li>updateFilter-Rest call for favorites</li>
                            <li>Finishing time sheet editing</li>
                            <li>Calendar events (espacially recurrences)</li>
                            <li>List pages: addresses, tasks etc.</li>
                            <li>Search filter</li>
                            <li>Favorites</li>
                            <li>Setup page</li>
                            <li>Switch between edit pages of time sheets and calendar events</li>
                            <li>
                                Edit of addresses: preserve address books without access of current
                                logged in user.
                            </li>
                            <li>Message of the day</li>
                            <li>System alert message</li>
                            <li>
                                Remove Apple-Control chars in all input fields (generated by copy
                                and paste e. g. from Apple&apos; address book)
                            </li>
                        </ol>
                    </Col>
                    <Col>
                        <h1>ToDo&apos;s (both)</h1>
                        <ol>
                            <li>Magic filter in list pages</li>
                            <li>List pagination and sorting</li>
                            <li>Highlight last edited entry in list page</li>
                            <li>
                                Registration of customized containers (e. g. for external plugins)
                            </li>
                        </ol>
                    </Col>
                </Row>
            </Container>
        );
    }
}

IndexPage.propTypes = {};

IndexPage.defaultProps = {};

export default IndexPage;
