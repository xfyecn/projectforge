import history from '../../utilities/history';
import { getObjectFromQuery, getServiceURL, handleHTTPErrors } from '../../utilities/rest';

export const EDIT_CALL_ACTION_BEGIN = 'EDIT_CALL_ACTION_BEGIN';
export const EDIT_CALL_ACTION_SUCCESS = 'EDIT_CALL_ACTION_SUCCESS';
export const EDIT_CALL_FAILURE = 'EDIT_CALL_FAILURE';
export const EDIT_CALL_INITIAL_BEGIN = 'EDIT_CALL_INITIAL_BEGIN';
export const EDIT_CALL_SUCCESS = 'EDIT_CALL_SUCCESS';
export const EDIT_CHANGE_DATA = 'EDIT_CHANGE_DATA';
export const EDIT_CHANGE_VARIABLES = 'EDIT_CHANGE_VARIABLES';
export const EDIT_SWITCH_CATEGORY = 'EDIT_SWITCH_CATEGORY';

const callActionBegin = category => ({
    type: EDIT_CALL_ACTION_BEGIN,
    payload: { category },
});

const callActionSuccess = category => ({
    type: EDIT_CALL_ACTION_SUCCESS,
    payload: { category },
});

const callFailure = (category, error) => ({
    type: EDIT_CALL_FAILURE,
    payload: {
        category,
        error,
    },
});

const callInitialBegin = (category, id) => ({
    type: EDIT_CALL_INITIAL_BEGIN,
    payload: {
        category,
        id,
    },
});

const callSuccess = (category, response) => ({
    type: EDIT_CALL_SUCCESS,
    payload: {
        category,
        response,
    },
});

const changeData = (category, newData) => ({
    type: EDIT_CHANGE_DATA,
    payload: {
        category,
        newData,
    },
});

const changeVariables = (category, newVariables) => ({
    type: EDIT_CHANGE_VARIABLES,
    payload: {
        category,
        newVariables,
    },
});

const switchCategoryWithData = (from, to, newVariables) => ({
    type: EDIT_SWITCH_CATEGORY,
    payload: {
        from,
        newVariables,
        category: to,
    },
});

export const loadEditPage = (category, { id, search }) => (dispatch, getState) => {
    const currentCategory = getState().edit.categories[category];

    if (currentCategory && currentCategory.isFetching) {
        return Promise.resolve();
    }

    dispatch(callInitialBegin(category, id));

    return fetch(
        getServiceURL(
            `${category}/edit`,
            {
                ...getObjectFromQuery(search || ''),
                id,
            },
        ),
        {
            method: 'GET',
            credentials: 'include',
        },
    )
        .then(handleHTTPErrors)
        .then(response => response.json())
        .then(json => dispatch(callSuccess(category, json)))
        .catch(error => callFailure(category, error));
};

export const callAction = ({ responseAction: action }) => (dispatch, getState) => {
    if (!action) {
        return Promise.reject(Error('No response action given.'));
    }

    const { edit: state } = getState();
    const category = state.currentCategory;

    dispatch(callActionBegin(category));

    let status = 0;

    const { data, watchFieldsTriggered } = state.categories[category];

    return fetch(
        getServiceURL(action.url),
        {
            method: action.targetType,
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                data,
                watchFieldsTriggered,
            }),
        },
    )
        .then((response) => {
            ({ status } = response);

            if (status === 200 || status === 406) {
                return response.json();
            }

            throw Error(`Error ${status}`);
        })
        .then((json) => {
            dispatch(callActionSuccess(category));

            switch (status) {
                case 200:
                    switch (json.targetType) {
                        case 'REDIRECT':
                            history.push(json.url, json.variables);
                            break;
                        case 'UPDATE':
                            if (json.url) {
                                history.push(
                                    `${json.url}`,
                                    {
                                        noReload: true,
                                        newVariables: json.variables,
                                    },
                                );
                                window.scrollTo(0, 0);
                            } else {
                                dispatch(callSuccess(category, json.variables));
                            }
                            break;
                        case 'NOTHING':
                        default:
                            throw Error(`Target Type ${json.targetType} not implemented.`);
                    }
                    break;
                case 406:
                    dispatch(callSuccess(category, { validationErrors: json.validationErrors }));
                    window.scrollTo(0, 0);
                    break;
                default:
                    throw Error(`Error ${status}`);
            }
        })
        .catch(error => dispatch(callFailure(category, error)));
};

export const setCurrentData = newData => (dispatch, getState) => dispatch(
    changeData(getState().edit.currentCategory, newData),
);

export const setCurrentVariables = newVariables => (dispatch, getState) => dispatch(
    changeVariables(getState().edit.currentCategory, newVariables),
);

export const switchFromCurrentCategory = (to, newVariables) => (dispatch, getState) => {
    const { edit: state } = getState();
    const from = state.currentCategory;

    dispatch(switchCategoryWithData(
        from,
        to,
        {
            ...state[from],
            ...newVariables,
        },
    ));
};
