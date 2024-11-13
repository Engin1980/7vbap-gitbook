import {SubmitHandler, useForm} from "react-hook-form";
import useHttp from "../../hooks/use-http";
import {toast} from "react-toastify";
import {MDBBtn, MDBCol, MDBContainer, MDBInput, MDBRow} from "mdb-react-ui-kit";
import {NavLink, useNavigate} from "react-router-dom";
import {useLoggedUser} from "../../hooks/use-logged-user";
import {AppUserView} from "../../model/app-user-view";

type Data = {
  email: string;
  password: string;
}

function Login() {
  const {
    register,
    handleSubmit,
    formState: {errors}
  } = useForm<Data>();
  const http = useHttp();
  const {login} = useLoggedUser();
  const navigate = useNavigate();

  const submitHandler: SubmitHandler<Data> = async data => {
    const formData = new FormData();
    formData.append("email", data.email);
    formData.append("password", data.password);

    try {
      const user = await http.post<AppUserView>("/v1/appUser/login", formData);
      login(user);
      toast.success("Logged in successfully.");
      navigate("/urls");
    } catch (err) {
      console.log(err);
      toast.error("Login failed.");
    }
  }

  return (<div>

    <MDBContainer>

      <MDBRow>
        <MDBCol md="6" className="mx-auto">
          <h1>Login</h1>
          <form onSubmit={handleSubmit(submitHandler)}>

            <MDBInput
              className="mb-3 mt-4"
              label="Email"
              type="email"
              {...register("email", {
                required: "This field is required."
              })}
            />
            {errors.email && (
              <span className="invalid-feedback d-block">
                          {errors.email.message}
                        </span>
            )}

            <MDBInput
              className="mb-3 mt-4"
              label="Password"
              type="password"
              {...register("password", {
                required: "This field is required"
              })} />
            {errors.password && (
              <span className="invalid-feedback d-block">
                        {errors.password.message}
                      </span>
            )}

            <MDBRow className="mt-5">
              <MDBCol>
                <MDBBtn type="submit">Log In</MDBBtn>
              </MDBCol>
              <MDBCol>
                <NavLink to="/register">Register</NavLink>
              </MDBCol>
            </MDBRow>
          </form>
        </MDBCol>
      </MDBRow>
    </MDBContainer>
  </div>);
}

export default Login;