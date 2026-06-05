/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect,  useContext, useRef} from "react";
import { AppContext } from "../../Context/Context";

const JDBCType = ({ exchangeConnector, setExchangeConnector, setIsContentValid, editMode, category }) => {
	const dummyDisplayPassword = "••••••••";
	const [ passwordInputValue, setPasswordInputValue ] = useState("");

	useEffect(() => {
		const developmentParams = exchangeConnector?.parametersContainer?.development || {};
		const { username = "", password = "", connectionString = "" } = developmentParams; 
        
		const isUsernameValid = username !== "";
		const isPasswordValid = password !== ""; 
		const isConnectionStringValid = connectionString !== "";

		setIsContentValid(isUsernameValid && isPasswordValid && isConnectionStringValid);
	}, [
		exchangeConnector?.parametersContainer?.development?.username,
		exchangeConnector?.parametersContainer?.development?.password,
		exchangeConnector?.parametersContainer?.development?.connectionString,
		setIsContentValid
	]);

	useEffect(() => {
		if (editMode) {
			setPasswordInputValue(dummyDisplayPassword);
		} else {
			setPasswordInputValue("");
			if (exchangeConnector?.parametersContainer?.development?.password !== "") {
				setExchangeConnector(prev => ({
					...prev,
					parametersContainer: {
						...prev.parametersContainer,
						development: {
							...prev.parametersContainer?.development,
							password: ""
						}
					}
				}));
			}
		}
	}, [ editMode, setExchangeConnector ]);

	const handleChange = (e) => {
		const { name, value } = e.target;

		if (name === "password" && editMode) {
			setPasswordInputValue(value);
		}

		setExchangeConnector(prev => ({
			...prev,
			parametersContainer: {
				...prev.parametersContainer,
				development: {
					...prev.parametersContainer?.development,
					[name]: value
				}
			}
		}));
	};

	const handlePasswordFocus = () => {
		if (editMode && passwordInputValue === dummyDisplayPassword) {
			setPasswordInputValue("");
		}
	};

	const handlePasswordBlur = () => {
		if (editMode) {
			if (passwordInputValue === "") {
				setPasswordInputValue(dummyDisplayPassword);
			}
		}
	};

	return (
		<div>
			<div className="row-container">
				<div className="data-input row-cell2">
					<input
						type="text"
						name="username"
						value={exchangeConnector?.parametersContainer?.development?.username || ""}
						onChange={handleChange}
						className="addConnEntity_connParam"
						placeholder="username"
						autoComplete="new-username"
					/>
					<label className={!exchangeConnector?.parametersContainer?.development?.username ? "item-with-error" : ""}>Username</label>
				</div>
				<div className="data-input row-cell2">
					<input
						type="password"
						name="password"
						id="passwordInput"
						value={
							editMode
								? passwordInputValue
								: exchangeConnector?.parametersContainer?.development?.password || ""
						}
						onChange={handleChange}
						onFocus={handlePasswordFocus}
						onBlur={handlePasswordBlur}
						className="addConnEntity_connParam"
						placeholder="password"
						autoComplete="new-password"
					/>
					<label className={!exchangeConnector?.parametersContainer?.development?.password ? "item-with-error" : ""}>Password</label>
				</div>
			</div>

			<div className="row-container">
				<div className="data-input row-cell1">
					<input
						type="text"
						onChange={handleChange}
						value={exchangeConnector?.parametersContainer?.development?.connectionString || ""}
						name="connectionString"
						className="addConnEntity_connParam"
						placeholder="connection string"
						autoComplete="off"
					/>
					<label className={!exchangeConnector?.parametersContainer?.development?.connectionString ? "item-with-error" : ""}>Connection string</label>
				</div>
			</div>

			<div className="row-container">
				<div className="data-input row-cell1">
					<input
						type="text"
						onChange={handleChange}
						value={exchangeConnector?.parametersContainer?.development?.[category === "Mongo" ? "dbName" : "schemas"] || ""}
						name={category === "Mongo" ? "dbName" : "schemas"}
						className="addConnEntity_connParam"
						placeholder={category === "Mongo" ? "database name" : "schema, schema, ..."}
						autoComplete="off"
					/>
					<label>
						{category === "Mongo" ? "Database Name" : category === "Oracle" ? "Extra Schemas" : "Schemas"}
					</label>
				</div>
			</div>
		</div>
	);
};

export default JDBCType;

