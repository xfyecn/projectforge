import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import React from 'react';
import { colorPropType } from '../../../utilities/propTypes';
import AdditionalLabel from './AdditionalLabel';
import style from './Input.module.scss';

const Input = React.forwardRef((
    {
        additionalLabel,
        className,
        color,
        icon,
        id,
        label,
        onBlur,
        onFocus,
        small,
        type,
        value,
        ...props
    },
    ref,
) => {
    const [active, setActive] = React.useState(false);

    const handleBlur = (event) => {
        if (onBlur) {
            onBlur(event);
        }

        setActive(event.target.value !== '');
    };

    const handleFocus = (event) => {
        if (onFocus) {
            onFocus(event);
        }

        setActive(true);
    };

    return (
        <div
            className={classNames(
                style.formGroup,
                'form-group',
                { [style.small]: small },
                className,
            )}
        >
            <label
                className={classNames(
                    style.label,
                    {
                        [style.active]: value || active,
                        [style.noLabel]: label === undefined,
                        [style.withIcon]: icon !== undefined,
                    },
                    style[color],
                )}
                htmlFor={id}
            >
                {icon && <FontAwesomeIcon icon={icon} className={style.icon} />}
                <input
                    ref={ref}
                    className={style.input}
                    type={type}
                    id={id}
                    {...props}
                    onBlur={handleBlur}
                    onFocus={handleFocus}
                    value={value}
                />
                <span className={style.text}>{label}</span>
            </label>
            <AdditionalLabel title={additionalLabel} />
        </div>
    );
});

Input.propTypes = {
    id: PropTypes.string.isRequired,
    additionalLabel: PropTypes.string,
    className: PropTypes.string,
    color: colorPropType,
    icon: PropTypes.shape({}),
    label: PropTypes.string,
    onBlur: PropTypes.func,
    onFocus: PropTypes.func,
    small: PropTypes.bool,
    type: PropTypes.string,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
};

Input.defaultProps = {
    additionalLabel: undefined,
    className: undefined,
    color: undefined,
    icon: undefined,
    label: undefined,
    onBlur: undefined,
    onFocus: undefined,
    small: false,
    type: 'text',
    value: undefined,
};

export default Input;
