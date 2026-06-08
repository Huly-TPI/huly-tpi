import { render, screen, fireEvent, act } from '@testing-library/react';
import {BreathingGuide} from '../../components/BreathingGuide';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';

vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({
    requireAuth: (action: () => void) => action(),
  }),
}))

vi.mock('../../context/theme', () => ({
  useTheme: () => ({
    theme: 'light',
  }),
}))

vi.mock('../../components/Buttons/BackButton/BackButton', () => ({
  default: () => null,
}))

describe('BreathingGuide', () => {

      afterEach(() => {
        vi.useRealTimers()
      })

      it('muestra botón para iniciar ejercicios tras seleccionar técnica', async () => {
        const user = userEvent.setup()
        render(<BreathingGuide />)
        await user.click(screen.getByRole('button', { name: /diafragmática/i }))
        expect(screen.getByRole('button', { name: /iniciar/i })).toBeInTheDocument()
      })
        
      it('muestra las técnicas de respiración disponibles', () => {
        render(<BreathingGuide />)
        expect(screen.getByText(/diafragmática/i)).toBeInTheDocument()
        expect(screen.getByText(/cuadrada/i)).toBeInTheDocument()
      })
      it('al seleccionar una tecnica oculta la selección y muestra el ejercicio', async () => {
          const user = userEvent.setup()
          render(<BreathingGuide />)
          await user.click(screen.getByRole('button', { name: /diafragmática/i }))
          expect(screen.queryByText(/tómate un momento/i)).not.toBeInTheDocument()
          })
      it('al iniciar el ejercicio muestra la primera fase', async () => {
        const user = userEvent.setup()
        render(<BreathingGuide />)
        await user.click(screen.getByRole('button', { name: /diafragmática/i }))
        await user.click(screen.getByRole('button', { name: /iniciar/i }))
        expect(screen.queryByText(/inhala profundamente/i)).not.toBeInTheDocument()
        expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
      })
      it('muestra el tiempo restante de la fase actual al iniciar el ejercicio', async () => { 
        render(<BreathingGuide />  )
        fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
        fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
        expect(screen.getByText(/4/i)).toBeInTheDocument()
      })
      it('el temporizador descuenta segundo por segundo', () => {
      vi.useFakeTimers()
        render(<BreathingGuide />  )
        fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
        fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
        expect(screen.getByText(/4/i)).toBeInTheDocument()
        act(() => {    vi.advanceTimersByTime(1000)
        } )
        expect(screen.getByText(/3/i)).toBeInTheDocument()
      vi.useRealTimers()
        })

        it('al finalizar una fase pasa a la siguiente', () => {
          vi.useFakeTimers()
            render(<BreathingGuide />  )
            fireEvent.click(screen.getByRole('button', { name: /cuadrada/i }))
            fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
            expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
            act(() => {    vi.advanceTimersByTime(1000)})
            act(() => {    vi.advanceTimersByTime(1000)})
            act(() => {    vi.advanceTimersByTime(1000)})
            act(() => {    vi.advanceTimersByTime(1000)})
            expect(screen.getByText(/sostenélo/i)).toBeInTheDocument()
          vi.useRealTimers()
            })
            
         
          it('al finalizar una ronda vuelve a la primera fase y repite el proceso', () => {
            vi.useFakeTimers()
              render(<BreathingGuide />  )
              fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
              fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
              expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
              for(let i = 0; i < 8; i++) {
                act(() => {    vi.advanceTimersByTime(1000)}  
                  )   } 
              expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
              })




          it('al hacer click en volver regresa a la selección de tecnicas', async () => {
            const user = userEvent.setup()
            render(<BreathingGuide />)
            await user.click(screen.getByRole('button', { name: /diafragmática/i }))
            await user.click(screen.getByRole('button', { name: /volver/i }))
            expect(screen.getByText(/tómate un momento/i)).toBeInTheDocument()
          })

         it('al terminar todas las rondas regresa a la selecciónd e técnicas', () => {
          const shortTechnique = {
            id: 3,
            name: 'Corta',
            description: 'Inhala por 1 segundo, sostene por 1 segundo, exhala por 1 segundo.',
            inhaleSeconds: 1,
            holdSeconds: 1,
            exhaleSeconds: 1,
            roundsInterval: 1,
            rounds: 2,
          }
          vi.useFakeTimers()
              render(<BreathingGuide techniques={[shortTechnique]} />)
              fireEvent.click(screen.getByRole('button', { name: /corta/i }))
              fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
              expect(screen.getByText(/inhalá/i)).toBeInTheDocument()
                
              for(let i = 0; i < 6; i++) {
                act(() => {    vi.advanceTimersByTime(1000)}  
                  )   } 
              expect(screen.getByText(/respiración guiada/i)).toBeInTheDocument()
              vi.useRealTimers()
            })

        it('muestra botón volver durante el ejercicio', async () => {
          render(<BreathingGuide />) 
          fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
          fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
          expect(screen.getByRole('button', { name: /volver/i })).toBeInTheDocument()
        })

        it('aplica la clase CSS correcta según la fase activa', async () => {
          render(<BreathingGuide />) 
          fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
          fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
          const circle = screen.getByTestId('breathing-circle')
          expect(circle).toHaveClass('inhale') 
          })

          it('pausa el ejercicio al hacer click en pausar', () => {
            vi.useFakeTimers()
            render(<BreathingGuide />)
            fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
            fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
             expect(screen.getByText('4')).toBeInTheDocument()
            fireEvent.click(screen.getByRole('button', { name: /pausar/i }))
            act(() => vi.advanceTimersByTime(2000))
            expect(screen.getByText('4')).toBeInTheDocument()
            })


            it('muestra la imagen de huly al inhalar', () => {
              vi.useFakeTimers()
              render(<BreathingGuide 
                hulyNormal="huly-normal.webp"
                hulyInhalando="huly-inhalando.webp"
                hulyExhalando="huly-exhalando.webp"
              />)
              fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
              fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
              const img = screen.getByAltText('Huly') 
              expect(img).toHaveAttribute('src', 'huly-inhalando.webp')
              vi.useRealTimers()
              })

        it('muestra la imagen de huly al exhalar', () => {
          vi.useFakeTimers()
          render(<BreathingGuide
            hulyNormal="huly-normal.webp"
            hulyInhalando="huly-inhalando.webp"
            hulyExhalando="huly-exhalando.webp"
          />)
          fireEvent.click(screen.getByRole('button', { name: /diafragmática/i }))
          fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
          for(let i = 0; i < 4; i++) {
            act(() => {    vi.advanceTimersByTime(1000)}  
              ) }
          const img = screen.getByAltText('Huly') 
          expect(img).toHaveAttribute('src', 'huly-exhalando.webp')
          vi.useRealTimers()
          })

          it('muestra la imagen de huly normal al sostener', () => {
            vi.useFakeTimers()
            render(<BreathingGuide
              hulyNormal="huly-normal.webp"
              hulyInhalando="huly-inhalando.webp"
              hulyExhalando="huly-exhalando.webp"
            />)
            fireEvent.click(screen.getByRole('button', { name: /cuadrada/i }))
            fireEvent.click(screen.getByRole('button', { name: /iniciar/i }))
            for(let i = 0; i < 4; i++) {
              act(() => {    vi.advanceTimersByTime(1000)}  
                )
              }
            const img = screen.getByAltText('Huly') 
            expect(img).toHaveAttribute('src', 'huly-normal.webp')
            vi.useRealTimers()
            })
        });
